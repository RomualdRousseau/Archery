""" Kernel
"""
import os
import sys
import json
import argparse

import numpy as np
import tensorflow as tf
from tensorflow.keras import Sequential, Model
from tensorflow.keras.layers import Input, Dense, Embedding, Flatten, Concatenate

EPOCHS = 10

def init_argparse():
    """ Parse arguments.
    """
    parser = argparse.ArgumentParser(
        usage="%(prog)s [OPTION] [FILE]...",
        description="Generate Any2json tag classifier model using tensorflow"
    )
    parser.add_argument(
        "-v", "--version", action="version",
        version = f"{parser.prog} version 1.0.0"
    )
    parser.add_argument(
        "-V", "--vocabulary", dest="vocabulary_size",
        required=True, help="size fo vocabulary"
    )
    parser.add_argument(
        "-s", "--shape", dest="input_shape",
        required=True, help="shape of the input vector"
    )
    parser.add_argument(
        "-t", "--train", dest="train_path",
        required=True, help="base path of the training files"
    )
    parser.add_argument(
        "-m", "--model", dest="model_path",
        required=True, help="destination path to save the model"
    )
    return parser


def prepare_data(train_path, input_shape):
    """ Load training and validation sets and return suitable format for tensorflow.
    """

    print(f"Using vector input shape {input_shape}")
    entity, name, context, label = np.cumsum([int(t) for t in input_shape.split(',')])

    print("Loading training set ...")
    with open(os.path.join(train_path.strip(), "training.json"), encoding="UTF-8") as user_file:
        training = json.load(user_file)
    train_inputs = [
        np.array([t[0:entity] for t in training]),
        np.array([t[entity:name] for t in training]),
        np.array([t[name:context] for t in training])
    ]
    train_labels = np.array([t[context:label] for t in training])

    print("Loading validation set ...")
    with open(os.path.join(train_path.strip(), "validation.json"), encoding="UTF-8") as user_file:
        validation = json.load(user_file)
    valid_inputs = [
        np.array([t[0:+entity] for t in validation]),
        np.array([t[entity:name] for t in validation]),
        np.array([t[name:context] for t in validation])
    ]
    valid_labels = np.array([t[context:label] for t in validation])

    return (train_inputs, train_labels, valid_inputs, valid_labels)


def build_model(input_shape, vocabulary_size):
    """ Build a tensorflow model from 3 inputs (entities, name, context).
    """

    print("Num GPUs Available: ", len(tf.config.experimental.list_physical_devices('GPU')))
    for device in tf.config.experimental.list_physical_devices('GPU'):
        device_details = tf.config.experimental.get_device_details(device)
        device_name = device_details['device_name']
        device_version = device_details['compute_capability']
        print(" %s: %s has compute capability %d.%d" % (device.name, device_name, device_version[0], device_version[1]))

    entity, name, context, label = [int(t) for t in input_shape.split(',')]

    input1 = Sequential([
        Input(shape=(entity,), name="entity_input")
        ])
    input2 = Sequential([
        Embedding(vocabulary_size, 8, input_length=name, name="name"),
        Flatten()
        ])
    input3 = Sequential([
        Embedding(vocabulary_size, 32, input_length=context, name="context"),
        Flatten()
        ])
    x = Concatenate()([input1.output, input2.output, input3.output])
    x = Dense(768, activation="relu")(x)
    x = Dense(128, activation="relu")(x)
    output = Dense(label, activation="softmax", name="tag_output")(x)
    model = Model(inputs=[input1.input, input2.input, input3.input], outputs=output)

    model.compile(optimizer="adam", loss="categorical_crossentropy", metrics=["accuracy"])
    print(model.summary())

    return model


def train_model(model, train_inputs, train_labels, val_inputs, val_labels):
    """ Train the model until accuracy on validation test is 1.
    """
    while True:
        # fit the model

        model.fit(x=train_inputs, y=train_labels, validation_data=(val_inputs, val_labels), epochs=EPOCHS, verbose=2)

        # evaluate the model

        loss, accuracy = model.evaluate(x=val_inputs, y=val_labels, verbose=2)
        if accuracy == 1:
            break


if __name__ == "__main__":
    parser = init_argparse()
    args = parser.parse_args()
    data = prepare_data(args.train_path, args.input_shape)
    model = build_model(args.input_shape, int(args.vocabulary_size) + 1)
    train_model(model, *data)
    model.save(args.model_path.strip())

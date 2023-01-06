#!/bin/sh

export TF_CPP_MIN_VLOG_LEVEL=3
export TF_CPP_MIN_LOG_LEVEL=3

. .venv/bin/activate
python main.py $@
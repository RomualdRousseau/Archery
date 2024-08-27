package com.github.romualdrousseau.any2json.classifier;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.StreamSupport;

import com.github.romualdrousseau.shuju.json.JSON;
import com.github.romualdrousseau.shuju.json.JSONObject;
import com.github.romualdrousseau.any2json.Header;
import com.github.romualdrousseau.any2json.Model;
import com.github.romualdrousseau.any2json.Table;
import com.github.romualdrousseau.any2json.TagClassifier;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;

public class LLMTagClassifier extends SimpleTagClassifier {

    private final static String OPENAI_KEY = System.getenv("OPENAI_KEY");

    public LLMTagClassifier(final Model model, final TagClassifier.TagStyle tagStyle) {
        super(model, tagStyle);
        this.service = new OpenAiService(OPENAI_KEY);
        this.tags = null;
    }

    @Override
    public void close() throws Exception {
        this.tags = null;
    }

    @Override
    public void updateModelData() {
    }

    @Override
    public String predict(final Table table, final Header header) {
        if (this.tags != null) {
            return this.tags.<String>get(header.getName()).orElse("none");
        }

        final var csvContent = new StringBuilder();
        table.headers().forEach(h -> csvContent.append(h.getName() + ","));
        csvContent.replace(csvContent.length() - 1, csvContent.length(), "\n");
        StreamSupport.stream(table.rows().spliterator(), false).limit(10).forEach(r -> {
            if (r.getNumberOfCells() > 0) {
                r.cells().forEach(c -> csvContent.append("\"" + c.getValue() + "\","));
                csvContent.replace(csvContent.length() - 1, csvContent.length(), "\n");
            }
        });

        final var definitions = String.join("\n", this.getModel().getData().getList("definitions"));

        final var content = String.join("\n", this.getResourceContent("/prompt-template.txt"));
        final var message = new ChatMessage("user", content
                .replace("{DEF}", definitions)
                .replace("{CSV}", csvContent.toString()));

        final var completionRequest = ChatCompletionRequest.builder()
                .messages(List.of(message))
                .model("gpt-3.5-turbo")
                .temperature(0.0)
                .build();
        this.service.createChatCompletion(completionRequest).getChoices().forEach(x -> {
            this.tags = JSON.objectOf(x.getMessage().getContent());
        });

        System.out.println(this.tags.toString(true));

        return this.tags.<String>get(header.getName()).orElse("none");
    }

    private List<String> getResourceContent(String resourceName) {
        try {
            final var resourceUrl = this.getClass().getResource(resourceName);
            assert resourceUrl != null : resourceName + " not found";
            return Files.readAllLines(Path.of(resourceUrl.toURI()));
        } catch (URISyntaxException | IOException x) {
            throw new RuntimeException(x);
        }
    }

    private OpenAiService service;
    private JSONObject tags;
}

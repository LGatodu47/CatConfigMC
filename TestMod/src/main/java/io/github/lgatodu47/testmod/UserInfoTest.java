package io.github.lgatodu47.testmod;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.github.lgatodu47.catconfig.ConfigOption;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.UUID;

record UserInfoTest(String name, UUID id, int age) {
    public record Option(String name, @Nullable UserInfoTest defaultValue) implements ConfigOption<UserInfoTest> {
        @Override
        public void write(JsonWriter writer, @NotNull UserInfoTest value) throws IOException {
            writer.beginObject();
            writer.name("name"); writer.value(value.name);
            writer.name("id"); writer.value(value.id.toString());
            writer.name("age"); writer.value(value.age);
            writer.endObject();
        }

        @Override
        public UserInfoTest read(JsonReader reader) throws IOException {
            String name, id;
            int age;

            reader.beginObject();
            reader.nextName(); name = reader.nextString();
            reader.nextName(); id = reader.nextString();
            reader.nextName(); age = reader.nextInt();
            reader.endObject();

            return new UserInfoTest(name, UUID.fromString(id), age);
        }
    }
}

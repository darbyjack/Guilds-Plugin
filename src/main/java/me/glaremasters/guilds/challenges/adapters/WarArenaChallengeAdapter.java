/*
 * MIT License
 *
 * Copyright (c) 2019 Glare
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.glaremasters.guilds.challenges.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import me.glaremasters.guilds.arena.Arena;

import java.io.IOException;
import java.util.UUID;

public class WarArenaChallengeAdapter extends TypeAdapter<Arena> {

    @Override
    public void write(JsonWriter out, Arena arena) throws IOException {
        out.beginObject();
        out.name("uuid");
        out.value(arena.getId().toString());
        out.name("name");
        out.value(arena.getName());
        out.endObject();
    }

    @Override
    public Arena read(JsonReader in) throws IOException {
        String arenaName = null;
        String arenaId = null;
        in.beginObject();
        while(in.hasNext()) {
            String name = in.nextName();
            if (name.equals("uuid")) {
                arenaId = in.nextString();
            }
            else if (name.equals("name")) {
                arenaName = in.nextString();
            }
            else {
                in.skipValue();
            }
        }
        in.endObject();
        return new Arena(UUID.fromString(arenaId), arenaName);
    }
}

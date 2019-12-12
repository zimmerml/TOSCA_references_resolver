package tosca;

/*-
 * #%L
 * TOSCA_RR
 * %%
 * Copyright (C) 2017 Stuttgart Uni, IAAS
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class MetaFile {

    // Metadata file location
    public static final String filename = "TOSCA-Metadata/TOSCA.meta";

    // Metadata header
    private String head;

    // container for Metadate elements
    public static class MetaEntry {
        public String name;
        public String type;

        public MetaEntry() {}

        public MetaEntry(final String newName, final String newType) {
            this.name = newName;
            this.type = newType;
        }
    }

    // Metadata elements
    public List<MetaEntry> meta;

    /**
     * Basic constructor
     */
    public MetaFile() {
        this.meta = new LinkedList<>();
    }

    /**
     * Read Metadata from CSAR unpacked to folder
     *
     * @param folder with CSAR content
     * @throws IOException
     */
    public void init(final String folder) throws IOException {
        this.head = "";
        this.meta.clear();
        final BufferedReader br = new BufferedReader(new FileReader(folder + filename));
        String line = null;
        boolean isHead = true;
        MetaEntry entry = new MetaEntry();
        while ((line = br.readLine()) != null) {
            // read header
            if (isHead) {
                if (!line.startsWith("Name:")) {
                    this.head += line + "\n";
                    continue;
                } else {
                    isHead = false;
                }
            }
            // read elements
            if (line.startsWith("Name:")) {
                String[] words = line.split("\\s+");
                entry.name = words[1];
                if ((line = br.readLine()) != null && line.startsWith("Content-Type:")) {
                    words = line.split("\\s+");
                    entry.type = words[1];
                    this.meta.add(entry);
                    entry = new MetaEntry();
                }
            }
        }
        br.close();
    }

    /**
     * Add new element to Metadata
     *
     * @param path to new file
     * @param type of new file
     */
    public void addFileToMeta(final String path, final String type) {
        final MetaEntry entry = new MetaEntry(path, type);
        if (!this.meta.contains(entry)) {
            this.meta.add(entry);
        }
    }

    /**
     * Pack Data back to file
     *
     * @param folder
     * @throws IOException
     */
    public void pack(final String folder) throws IOException {
        final FileWriter bw = new FileWriter(folder + filename);
        bw.write(this.head);
        for (final MetaEntry entry : this.meta) {
            bw.write("Name: " + entry.name + "\nContent-Type: " + entry.type + "\n\n");
        }
        bw.flush();
        bw.close();
    }

}

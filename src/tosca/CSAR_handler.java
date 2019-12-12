package tosca;

/*-
 * #%L
 * TOSCA_RR
 * %%
 * Copyright (C) 2017 Universität Stuttgart, IAAS
 * %%
 * Licensed under the Eclipse Public License v1.0 (the "License1") and the Apache License, Version 2.0 (the "License2");
 * you may not use this file except in compliance with the License1 or License2.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 * You may obtain a copy of the License1 at
 *
 *     https://eclipse.org/org/documents/epl-v10.php
 *
 * You may obtain a copy of the License2 at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License2 is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License2 for the specific language governing permissions and
 * limitations under the License2.
 * #L%
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Scanner;

import javax.xml.bind.JAXBException;

import tosca.Abstract.Language;

// unpack
/**
 * @author jery
 *
 */
public class CSAR_handler {

    // input CSAR file name
    private String CSAR;

    // folder containing extracted files
    private String folder;

    // extracted files
    private List<String> files;

    // architecture of packages
    private String architecture;

    // Metafile description
    public MetaFile metaFile;

    // Download and proceed packets
    private final Package_Handler packet_handler;

    // Updates service templates
    public Topology_Handler service_template;

    public static final String ArchitectureFileName = "arch";
    public static final String Definitions = "Definitions/";

    public Boolean debug = false;

    public static enum Resolving {
        Mirror, Single, Expand, Archive
    }

    private Resolving resolving;

    public Resolving getResolving() {
        return this.resolving;
    }

    /**
     * Download and add packet to csar
     *
     * @param packet name to download
     * @return
     * @throws JAXBException
     * @throws IOException
     */
    public List<String> getPacket(final Language language, final String packet,
                                  final String source) throws JAXBException, IOException {
        return this.packet_handler.getPacket(language, packet, source);
    }

    /**
     * Update Service Template
     *
     * @param reference to script, which downloads packet
     * @param packet to be added to TOSCA
     * @throws JAXBException
     * @throws IOException
     */
    public void AddDependenciesScript(final String reference, final String packet) throws JAXBException, IOException {
        this.service_template.addDependencyToScript(reference, packet);
    }

    /**
     * Update service template
     *
     * @param source packet, which needs target packet
     * @param target new packet needed by source
     * @throws JAXBException
     * @throws IOException
     */
    public void AddDependenciesPacket(final String source, final String target,
                                      final String dependencyType) throws JAXBException, IOException {
        this.service_template.addDependencyToPacket(source, target, dependencyType);
    }

    public void expandTOSCA_Node(final List<String> packages, final String source) throws IOException, JAXBException {
        this.service_template.expandTOSCA_Nodes(packages, source);
    }

    /**
     * init system
     *
     * @param filename CSAR archive
     * @throws IOException
     */
    public CSAR_handler(final String filename) throws FileNotFoundException, IOException {
        this.metaFile = new MetaFile();
        init(filename);
        this.packet_handler = new Package_Handler(this);
        this.service_template = new Topology_Handler(this);
    }

    /**
     * extract archive and read architecture
     *
     * @param filename CSAR archive
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void init(final String filename) throws FileNotFoundException, IOException {
        if (filename == null) {
            throw new NullPointerException();
        }
        this.CSAR = filename;
        unpack();
        readArchitecture();
        chooseResolving();

        final Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        System.out.println("START: " + timestamp);
    }

    /**
     * List extracted files
     *
     * @return list with files
     */
    public List<String> getFiles() {
        // List<String> fullFiles = new LinkedList<String>();
        // for (String s : files)
        // fullFiles.add(folder + s);
        return this.files;
    }

    /**
     * Get folder containing extracted files
     *
     * @return folder name
     */
    public String getFolder() {
        return this.folder;
    }

    /**
     * Unpack CSAR
     *
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void unpack() throws FileNotFoundException, IOException {
        this.folder = this.CSAR + "_temp_references_resolver";
        final File folderfile = new File(this.folder);
        this.folder = folderfile + File.separator;
        zip.delete(new File(this.folder));
        this.files = zip.unZipIt(this.CSAR, this.folder);
        this.metaFile.init(this.folder);
    }

    /**
     * Pack changed CSAR back to zip
     *
     * @param filename target archive filename
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void pack(final String filename) throws FileNotFoundException, IOException {
        this.metaFile.pack(this.folder);
        if (filename == null) {
            throw new NullPointerException();
        }
        zip.zipIt(filename, this.folder);
    }

    /**
     * Get archive filename
     *
     * @return archive filename
     */
    public String getCSARname() {
        return this.CSAR;
    }

    /**
     * Get current architecture
     *
     * @return architecture
     */
    public String getArchitecture() {
        return this.architecture;
    }

    /**
     * reads Architecture from extracted data or from user input
     *
     * @throws IOException
     */
    // no need to close user input
    @SuppressWarnings("resource")
    public void readArchitecture() throws IOException {
        final File arch = new File(this.folder + Resolver.folder + ArchitectureFileName);
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(arch));
            final String line = br.readLine();
            br.close();
            if (line != null && !line.equals("")) {
                this.architecture = line;
            } else {
                new File(this.folder + Resolver.folder + ArchitectureFileName).delete();
                throw new FileNotFoundException();
            }

        }
        catch (final FileNotFoundException e) {
            new File(this.folder + Resolver.folder).mkdir();
            final FileWriter bw = new FileWriter(arch);
            System.out.println("Please enter the architecure. (default: i386)");
            System.out.println("Example: i386, amd64, arm, noarch.");
            System.out.print("architecture: ");
            this.architecture = new Scanner(System.in).nextLine();
            if (this.architecture.equals("")) {
                this.architecture = "i386";
            }
            this.architecture = ":" + this.architecture;
            if (this.architecture.equals(":noarch")) {
                this.architecture = "";
            }
            bw.write(this.architecture);
            bw.close();
        }
        this.metaFile.addFileToMeta(Resolver.folder + ArchitectureFileName, "text/txt");
    }

    /**
     * Set specific architecture
     *
     * @param arch
     * @throws IOException
     */
    public void setArchitecture(final String arch) throws IOException {
        if (arch == null) {
            throw new NullPointerException();
        }
        this.architecture = arch;

        // delete old file
        final File fArch = new File(this.folder + Resolver.folder + ArchitectureFileName);
        fArch.delete();

        // create new file
        final FileWriter bw = new FileWriter(fArch);
        bw.write(arch);
        bw.flush();
        bw.close();
    }

    public void chooseResolving() {
        System.out.println("Please select the type of resolving");
        System.out.println("Supported modes: single, mirror, expand, archive. (default: archive)");
        System.out.print("resolving: ");
        final String temp = new Scanner(System.in).nextLine();
        if (temp.equals("single")) {
            this.resolving = Resolving.Single;
            System.out.println("Resolving accepted: single");
        } else if (temp.equals("mirror")) {
            this.resolving = Resolving.Mirror;
            System.out.println("Resolving accepted: mirror");
        } else if (temp.equals("expand")) {
            this.resolving = Resolving.Expand;
            System.out.println("Resolving accepted: expand");
        } else {
            this.resolving = Resolving.Archive;
            System.out.println("Resolving accepted: Archive");
        }
    }
}

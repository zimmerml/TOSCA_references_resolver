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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import javax.xml.bind.JAXBException;

import tosca.Abstract.Language;
import tosca.Languages.Ansible.Ansible;
import tosca.Languages.Bash.Bash;
import tosca.Languages.Docker.Dockerfile;

/**
 * @author jery
 *
 */
public class Resolver {

    // Folder Name, used by this framework
    static public final String folder = "References_resolver" + File.separator;

    // Active languages
    List<Language> languages;

    /**
     * Test, creates resolver with Bash language
     *
     * @param args
     * @throws IOException
     */
    public static void main(final String[] args) throws IOException {

        String source, target;
        final Resolver resolver = new Resolver();
        if (args.length >= 1) {
            source = args[0];
        } else {
            System.out.print("enter the input CSAR name (default: example.csar): ");
            source = new Scanner(System.in).nextLine();
            if (source.equals("")) {
                source = "example.csar";
            }
        }
        if (args.length >= 2) {
            target = args[1];
        } else {
            System.out.print("enter the output CSAR name (default: newexample.csar): ");
            target = new Scanner(System.in).nextLine();
            if (target.equals("")) {
                target = "newexample.csar";
            }
        }
        System.out.println("source: " + source);
        System.out.println("target: " + target);
        resolver.proceedCSAR(source, target);

        final Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        System.out.println("END: " + timestamp);
    }

    /**
     * Constructor
     */
    public Resolver() {
        this.languages = new LinkedList<>();
    }

    /**
     * proceed CSAR
     *
     * @param filename input CSAR name
     * @param output output CSAR name
     * @throws IOException
     */
    public void proceedCSAR(final String filename, final String output) throws IOException {
        if (filename == null || output == null) {
            throw new NullPointerException();
        }

        System.out.println("Proceeding file " + filename);
        CSAR_handler ch;
        try {
            // create CSAR manager and unpack archive
            ch = new CSAR_handler(filename);

            // init Languages
            this.languages.add(new Ansible(ch));
            this.languages.add(new Bash(ch));
            this.languages.add(new Dockerfile(ch));
            /*
             * TODO Node type. done Artifact Type for package. done Relationship Type for dependencies change
             * service template
             */
            // new File(ch.getFolder() + CSAR_handler.Definitions).mkdirs();
            // RR_PackageArtifactType.init(ch);
            // RR_ScriptArtifactType.init(ch);
            // RR_AnsibleArtifactType.init(ch);
            // RR_PreDependsOn.init(ch);
            // RR_DependsOn.init(ch);
        }
        catch (final FileNotFoundException e) {
            System.out.println("Error by unpacking " + filename + ", file not found");
            return;
        }
        // proceed all extracted files using each available language
        try {
            for (final Language l : this.languages) {
                l.proceed();
            }
        }
        catch (final JAXBException e1) {
            System.out.println("Unable to create xml annotation to package");
            e1.printStackTrace();
        }
        // pack CSAR
        try {
            ch.pack(output);
            System.out.println("packed to: " + output);
        }
        catch (final FileNotFoundException e) {
            System.out.println("File: not found during packing to: " + output);
            return;
        }
    }
}

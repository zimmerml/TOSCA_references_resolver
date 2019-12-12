package tosca.Abstract;

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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBException;

import tosca.CSAR_handler;
import tosca.Utils;

public abstract class Language {

    // List of package managers supported by language
    protected List<PackageManager> packetManagers;

    // Extensions for this language
    protected List<String> extensions;

    // Language Name
    protected String Name;

    // To access package topology
    protected CSAR_handler ch;

    // List with already created packages
    protected List<String> created_packages;

    /**
     * get Language name
     *
     * @return
     */
    public String getName() {
        return this.Name;
    }

    /**
     * Get supported extensions
     *
     * @return list with extensions
     */
    public List<String> getExtensions() {
        return this.extensions;
    }

    /**
     * Proceed file, transfer it to package managers
     *
     * @param new_ch CSAR manager
     * @throws FileNotFoundException
     * @throws IOException
     * @throws JAXBException
     */
    public void proceed() throws FileNotFoundException, IOException, JAXBException {
        if (this.ch == null) {
            throw new NullPointerException();
        }
        for (final String f : this.ch.getFiles()) {
            for (final String suf : this.extensions) {
                if (f.toLowerCase().endsWith(suf.toLowerCase())) {
                    final List<String> packages = new LinkedList<>();
                    for (final PackageManager pm : this.packetManagers) {
                        packages.addAll(pm.proceed(this.ch.getFolder() + f, f));
                    }
                    if (packages.size() > 0 && this.ch.getResolving() == CSAR_handler.Resolving.Single) {
                        final List<String> templist = new LinkedList<>();
                        for (final String temp : packages) {
                            templist.add(Utils.correctName(temp));
                        }
                        createTOSCA_Node(templist, f);
                        this.ch.AddDependenciesScript(Utils.correctName(f), getNodeName(f));
                    }
                }
            }
        }
    }

    /**
     * Generate node name for specific packages
     *
     * @param packet
     * @param source
     * @return
     */
    public String getNodeName(final String packet, final String source) {
        return Utils.correctName(this.Name + "_" + packet + "_" + source.replace("/", "_"));
    }

    /**
     * Generate node name for specific packages
     *
     * @param source
     * @return
     */
    public String getNodeName(final String source) {
        return Utils.correctName(this.Name + "_for_" + source.replace("/", "_"));

    }

    /**
     * Generate Node for TOSCA Topology
     *
     * @param packet
     * @param source
     * @return
     * @throws IOException
     * @throws JAXBException
     */
    public abstract String createTOSCA_Node(String packet, String source) throws IOException, JAXBException;

    public abstract String createTOSCA_Node(List<String> packages, String source) throws IOException, JAXBException;

    public void expandTOSCA_Node(final List<String> packages, final String source) throws IOException, JAXBException {
        this.ch.expandTOSCA_Node(packages, Utils.correctName(source.replace("/", "_")));
    }
}

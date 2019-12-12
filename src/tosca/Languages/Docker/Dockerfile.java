package tosca.Languages.Docker;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBException;

import tosca.CSAR_handler;
import tosca.Abstract.Language;

public class Dockerfile extends Language {

    /**
     * Constructor list extensions
     *
     */
    public Dockerfile(final CSAR_handler new_ch) {
        this.ch = new_ch;
        this.Name = "Docker";
        this.extensions = new LinkedList<>();
        this.extensions.add("docker.zip"); // convention: dockerfile is contained in a *.zip file containing
                                           // "docker.zip" as name

        this.created_packages = new LinkedList<>();

        this.packetManagers = new LinkedList<>();
        this.packetManagers.add(new Docker(this, this.ch));
    }


    @Override
    public String createTOSCA_Node(final String packet, final String source) throws IOException, JAXBException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String createTOSCA_Node(final List<String> packages, final String source) throws IOException, JAXBException {
        // TODO Auto-generated method stub
        return null;
    }

}

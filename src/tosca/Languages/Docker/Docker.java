package tosca.Languages.Docker;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;

import tosca.CSAR_handler;
import tosca.MetaFile.MetaEntry;
import tosca.zip;
import tosca.Abstract.Language;
import tosca.Abstract.PackageManager;

public class Docker extends PackageManager {

    // package manager name
    static public final String Name = "Docker";

    public Docker(final Language language, final CSAR_handler new_ch) {
        this.language = language;
        this.ch = new_ch;
    }

    @Override
    public List<String> proceed(final String filename, final String source) throws FileNotFoundException, IOException,
                                                                            JAXBException {
        if (this.ch == null) {
            throw new NullPointerException();
        }
        final List<String> output = new LinkedList<>();
        System.out.println(Name + " proceed " + filename);

        final String folder = filename.substring(0, filename.lastIndexOf("/"));

        System.out.println("FOLDER: " + folder);

        final List<String> zipFiles = zip.unZipIt(filename, folder.concat("/"));

        if (zipFiles.contains("Dockerfile")) {

            System.out.println("Dockerfile found! Trying to create the docker image.");

            final String dockerImageName = filename.substring(filename.lastIndexOf("/") + 1, filename.lastIndexOf("."));

            if (executeTask(folder, "docker", "build", "-t", dockerImageName, ".")) {

                System.out.println("Docker image " + dockerImageName + " created. Saving it as file...");

                if (executeTask(folder, "docker", "save", "-o", dockerImageName + ".tar",
                                dockerImageName + ":latest")) {

                    System.out.println("Docker image " + dockerImageName + " saved as file. Compressing it...");

                    final String compressedFile = compressTarFile(new File(folder + "\\" + dockerImageName + ".tar"));

                    if (compressedFile != null) {

                        final File folderFile = new File(folder);

                        System.out.println("Docker image compressed: " + compressedFile);
                        output.add(dockerImageName + ".tar.gz");

                        zip.deleteFilesInFolder(folderFile, "gz");

                        System.out.println("Adapting TOSCA files referencing this Docker container...");
                        final String folderPath = folderFile.getParent();
                        final String DAFolderName = folderPath.substring(folderPath.lastIndexOf("\\") + 1);
                        final String root = folder.substring(0, folder.indexOf("\\"));

                        adaptTOSCAFiles(filename.substring(filename.lastIndexOf("/") + 1), dockerImageName + ".tar.gz",
                                        DAFolderName, root);


                    } else {
                        System.out.println("Compressing failed.");
                    }
                } else {
                    System.out.println("Saving docker image as file failed.");
                }
            } else {
                System.out.println("Creating docker image failed.");
            }
        } else {
            System.out.println("No Dockerfile found!");
        }
        return output;
    }


    private boolean executeTask(final String directoryPath, final String... command) {
        Process process;
        final ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(new File(directoryPath));

        pb.redirectOutput(Redirect.INHERIT);
        pb.redirectError(Redirect.INHERIT);
        pb.redirectInput(Redirect.INHERIT);

        try {
            process = pb.start();
            if (0 == process.waitFor()) {
                return true;
            }
        }
        catch (IOException | InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

    private String compressTarFile(final File tarFile) {

        String fileName = null;

        try {
            final InputStream in = Files.newInputStream(Paths.get(tarFile.getAbsolutePath()));
            final OutputStream fout = Files.newOutputStream(Paths.get(tarFile.getAbsolutePath() + ".gz"));
            final BufferedOutputStream out = new BufferedOutputStream(fout);
            final GzipCompressorOutputStream gzOut = new GzipCompressorOutputStream(out);
            final byte[] buffer = new byte[4096];
            int n = 0;
            while (-1 != (n = in.read(buffer))) {
                gzOut.write(buffer, 0, n);
            }
            gzOut.close();
            in.close();
            fileName = tarFile.getName() + ".gz";
        }
        catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return fileName;

    }


    private void adaptTOSCAFiles(final String oldFileName, final String newFileName, final String DAfolderName,
                                 final String root) {

        final List<MetaEntry> metaFiles = this.ch.metaFile.meta;

        for (final MetaEntry entry : metaFiles) {
            if (entry.name.contains(oldFileName)) {
                entry.name = entry.name.replace(oldFileName, newFileName);
                break;
            }
        }

        final File DefinitionFile = new File(root + "\\Definitions\\artifacttemplates__" + DAfolderName + ".tosca");
        replaceStringInFile(DefinitionFile, oldFileName, newFileName);

    }

    private void replaceStringInFile(final File fileToBeModified, final String oldFileName, final String newFileName) {

        System.out.println("Adapting...: " + fileToBeModified.getAbsolutePath());

        String oldContent = "";
        BufferedReader reader = null;
        FileWriter writer = null;

        try {
            reader = new BufferedReader(new FileReader(fileToBeModified));

            String line = reader.readLine();

            while (line != null) {
                oldContent = oldContent + line + System.lineSeparator();

                line = reader.readLine();
            }

            final String newContent = oldContent.replaceAll(oldFileName, newFileName);

            writer = new FileWriter(fileToBeModified);
            reader.close();
            writer.write(newContent);
            writer.close();
        }
        catch (final IOException e) {
            e.printStackTrace();
        }
    }


}

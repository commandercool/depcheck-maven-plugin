package com.github.commandercool;

import com.github.commandercool.utils.MD5Generator;
import com.github.commandercool.utils.PathCompiler;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by Alex on 18.10.2016.
 */
@Mojo(name = "check", defaultPhase = LifecyclePhase.VALIDATE)
public class CheckingMojo extends AbstractMojo {

    @Parameter(defaultValue = "hash.md5")
    private String input;
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject mavenProject;
    @Parameter(defaultValue = "${localRepository}", readonly = true)
    private ArtifactRepository localRepository;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        input = PathCompiler.getAbsolutePath(input, mavenProject.getFile().getParent());
        try (BufferedReader in = new BufferedReader(new FileReader(input))) {
            String line;
            while ((line = in.readLine()) != null) {
                getLog().info(line);
                String[] contents = line.split(" ");
                Artifact artifact = getArtifact(contents[1]);
                if (artifact != null) {
                    File file = localRepository.find(artifact).getFile();
                    if (file == null) {
                        throw new MojoExecutionException(String.format("No file found for artifact %s. " +
                                "Try running compile before check.", artifact));
                    }
                    if (!MD5Generator.generateChecksum(file.getAbsolutePath())
                            .equals(contents[0].trim())) {
                        throw new MojoFailureException(String.format("Mismatch in checksum for artifact %s", contents[1]));
                    }
                } else {
                    throw new MojoExecutionException(String.format("No artifact found for id %s", contents[1]));
                }
            }
        } catch (IOException ex) {
            throw new MojoExecutionException(String.format("Exception reading checksum file %s", input));
        }
        getLog().info("Checksums are verified successfully");
    }

    private Artifact getArtifact(String id) {
        for (Artifact artifact : mavenProject.getDependencyArtifacts()) {
            if (artifact.getId().equals(id)) {
                return artifact;
            }
        }
        return null;
    }
}

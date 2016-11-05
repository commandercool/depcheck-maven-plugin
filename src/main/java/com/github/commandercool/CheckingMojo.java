package com.github.commandercool;

import com.github.commandercool.utils.MD5Generator;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.BufferedReader;
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

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try (BufferedReader in = new BufferedReader(new FileReader(input))) {
            String line;
            while ((line = in.readLine()) != null) {
                String[] contents = line.split(" ");
                if (!MD5Generator.generateChecksum(getArtifact(contents[1]).getFile().getAbsolutePath())
                        .equals(contents[0].trim())) {
                    throw new MojoFailureException("Mismatch in checksum for artifact " + contents[1]);
                }
            }
        } catch (IOException ex) {
            throw new MojoExecutionException("Exception reading checksum file " + input);
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

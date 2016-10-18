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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by Alex on 16.10.2016.
 */
@Mojo(name = "hash", defaultPhase = LifecyclePhase.COMPILE)
public class HashingMojo extends AbstractMojo {

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    @Parameter(defaultValue = "hash.md5")
    private String output;
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject mavenProject;

    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("Calculating checksums for the artifacts");
        try (BufferedWriter out = new BufferedWriter(new FileWriter(output))) {
            for (Artifact artifact : mavenProject.getDependencyArtifacts()) {
                try {
                    String outEntry = MD5Generator.generateChecksum(artifact.getFile().getAbsolutePath()) + " " +
                            artifact.getId();
                    out.write(outEntry);
                    out.write(LINE_SEPARATOR);
                    getLog().info(outEntry);
                } catch (IOException e) {
                    throw new MojoExecutionException("Exception reading artifact jar path: " +
                            artifact.getFile().getAbsolutePath(), e);
                }
            }
        } catch (IOException ex) {
            throw new MojoExecutionException("Unable to create output file", ex);
        }
        getLog().info("Checksums writed out to " + output);
    }
}
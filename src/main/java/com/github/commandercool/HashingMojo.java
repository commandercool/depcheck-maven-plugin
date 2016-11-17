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

import java.io.BufferedWriter;
import java.io.File;
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
    @Parameter(defaultValue = "${localRepository}", readonly = true)
    private ArtifactRepository localRepository;

    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("Calculating checksums for the artifacts");
        output = PathCompiler.getAbsolutePath(output, mavenProject.getFile().getParent());
        try (BufferedWriter out = new BufferedWriter(new FileWriter(output))) {
            for (Artifact artifact : mavenProject.getDependencyArtifacts()) {
                File file = localRepository.find(artifact).getFile();
                if (file == null) {
                    throw new MojoExecutionException(String.format("No file was found for artifact %s",
                            artifact.getId()));
                }
                if (!isInherited(artifact)) {
                    try {
                        String outEntry = String.format("%s %s", MD5Generator.generateChecksum(file.getAbsolutePath()),
                                artifact.getId());
                        out.write(outEntry);
                        out.write(LINE_SEPARATOR);
                        getLog().info(outEntry);
                    } catch (IOException e) {
                        throw new MojoExecutionException(String.format("Exception reading artifact jar path: %s",
                                file.getAbsolutePath()), e);
                    }
                }
            }
        } catch (IOException ex) {
            throw new MojoExecutionException("Unable to create output file", ex);
        }
        getLog().info(String.format("Checksums are written out to %s", output));
    }

    private boolean isInherited(Artifact artifact) {
        MavenProject parentProject = mavenProject;
        while ((parentProject = parentProject.getParent()) != null) {
            if (parentProject.getDependencyArtifacts().contains(artifact)) {
                return true;
            }
        }
        return false;
    }
}

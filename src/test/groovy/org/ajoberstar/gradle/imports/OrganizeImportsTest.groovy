package org.ajoberstar.gradle.imports

import java.nio.file.Files
import java.util.regex.Pattern

import spock.lang.Specification
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder

class OrganizeImportsTest extends Specification {
  @Rule TemporaryFolder tempDir = new TemporaryFolder()
  private String inputContents = '''\
package com.nortal.assignment.model;

import lombok.Data;
import java.util.Date;

@Data
public class Unicorn {
    private int id;
    private String name;
    private String gender;
    private Date birthdate;
    private int grassland_id;
}
'''

  private String outputContents = '''\
package com.nortal.assignment.model;

import java.util.Date;

import lombok.Data;

@Data
public class Unicorn {
    private int id;
    private String name;
    private String gender;
    private Date birthdate;
    private int grassland_id;
}
'''

  def 'two element fq-class names are sorted correctly'() {
    given:
    def sourcePath = tempDir.newFile('Unicorn.java').toPath()
    Files.write(sourcePath, inputContents.bytes)
    def task = ProjectBuilder.builder().build().task('organizeImports', type: OrganizeImports)
    when:
    task.organizeFile(sourcePath.toFile(), task.sortOrder.collect { Pattern.compile(it) })
    then:
    new String(Files.readAllBytes(sourcePath)) == outputContents
  }
}

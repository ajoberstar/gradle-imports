/*
 * Copyright 2014-2015 the original author or authors.
 *
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
 */
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
    new String(Files.readAllBytes(sourcePath)) == outputContents.denormalize()
  }
}

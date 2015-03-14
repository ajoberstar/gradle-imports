# gradle-imports

A Gradle plugin to organize imports in Java and Groovy source files.

[ ![Download](https://api.bintray.com/packages/ajoberstar/gradle-plugins/org.ajoberstar%3Agradle-imports/images/download.svg) ](https://bintray.com/ajoberstar/gradle-plugins/org.ajoberstar%3Agradle-imports/_latestVersion)
[![Stories in Ready](https://badge.waffle.io/ajoberstar/gradle-imports.png?label=ready&title=Ready)](https://waffle.io/ajoberstar/gradle-imports)

## Where do I get it?

See the [Gradle plugin portal](http://plugins.gradle.org/plugin/org.ajoberstar.organize-imports) for instructions on applying the latest version of the plugin.

## What does it do?

The plugin adds a single task called `organizeImports`. By default, the task is configured
to process all `.java` and `.groovy` files in all source sets.

It has two main behaviors:

- Sort imports into sections.
- Remove unused imports.

## How do I use it?

**NOTE:** You should *only* run this on **versioned** code. This will ensure that you can revert if you either don't
like the behavior or find a bug.

Just run the `organizeImports` task.

```
./gradlew organizeImports
```

## How can I configure it?

Just directly configure the task:

```groovy
organizeImports {
	sourceSets = project.sourceSets
	sortOrder = [/^(javax?)\./, /^(groovyx?)\./, /^([^\.]+\.[^\.]+)\./]
	staticImportsFirst = true
	removeUnused = true
	includes = ['**/*.java', '**/*..groovy']
}
```

The main one that needs explanation is the sort order. This is based on a list of regular expressions that match
against the fully qualified class names. It should have one capture group that returns the section of the class name
that will be sorted on.

## How does it work?

The sorting is done in 4 steps:
1. Static import or not.
1. Index of the `sortOrder` pattern it matched against.
1. Result of capture group of matched pattern.
1. Full class name

As an example (using the default `sortOrder`). The following imports:

```groovy
import groovy.transform.Immutable
import org.ajoberstar.grgit.Grgit
import org.ajoberstar.grgit.Commit
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.Repository
import java.util.regex.Matcher
import java.io.File
import static org.junit.Assert.*
```

Result in:

```groovy
import static org.junit.Assert.*

import java.io.File
import java.util.regex.Matcher

import groovy.transform.Immutable

import org.ajoberstar.grgit.Commit
import org.ajoberstar.grgit.Grgit

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.Repository
```

The approach to determining unused imports is to search for uses of the class's simple name after the import
declarations in the file. As mentioned above, this isn't meant to be a robust parser, so ensure you have your files
versioned before running it.

See the [Groovydoc](http://ajoberstar.org/gradle-imports/docs/groovydoc/) for more information.

## Release Notes

### v1.0.1

- Support ! in front of class name.

### v1.0.0

- Changing plugin ID from `organize-imports` to `org.ajoberstar.organize-imports` to be compatible with plugin portal.
- Waiting to configure sourceSets on task until the `java` plugin applied to work around ordering issues.

### v0.1.0

- Initial release.
- See this [commit](https://github.com/ajoberstar/grgit/commit/24e26d13431cf0e97c6762a281a2c7c84cafea23) for an example of the changes it makes.

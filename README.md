# gradle-imports

A Gradle plugin to organize imports in Java and Groovy source files.

## Where do I get it?

The package is published on [Bintray](https://bintray.com/ajoberstar/gradle-plugins/org.ajoberstar%3Agradle-imports)
and will be available on JCenter and Maven Central soon.

```groovy
buildscript {
	repositories {
		jcenter()
		// mavenCentral()
	}

	dependencies {
		classpath 'org.ajoberstar:gradle-imports:<version>'
	}
}

apply plugin: 'organize-imports'
```

## What does it do?

The plugin adds a single task called `organizeImports`. By default, the task is configured
to process all `.java` and `.groovy` files in all source sets.

It will cover two main tasks.

- Sort imports into sections.
- Remove unused imports.

NOTE: This is a quick and dirty plugin. I was able to test it on one of my larger projects and didn't have any issues.
That doesn't mean I thought of all of the edge cases. Please use this with care.

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

## How do I use it?

**NOTE:** You should *only* run this on **versioned** code. This will ensure that you can revert if you either don't
like the behavior or find a bug.

Just run the `organizeImports` task.

```
./gradlew organizeImports
```

## Release Notes

**v0.1.0**

- Initial release

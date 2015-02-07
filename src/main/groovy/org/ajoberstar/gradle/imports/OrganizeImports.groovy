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

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.util.PatternFilterable
import org.gradle.api.tasks.util.PatternSet

import java.util.regex.Matcher
import java.util.regex.Pattern
import groovy.transform.Immutable

class OrganizeImports extends DefaultTask implements PatternFilterable {
	private static final Pattern IMPORT_PATTERN = ~/^import(\s+static)?\s+(\S+\.([^\s;]+);?)/

	@Delegate private PatternFilterable patterns = new PatternSet()
	Set<SourceSet> sourceSets = []
	List<String> sortOrder = [/^(javax?)\./, /^(groovyx?)\./, /^([^\.]+\.[^\.]+)\./]
	boolean staticImportsFirst = true
	boolean removeUnused = true

	OrganizeImports() {
		patterns.include '**/*.java', '**/*.groovy'
	}

	@TaskAction
	void organize() {
		FileCollection allFiles = sourceSets.inject(getProject().files()) { allSource, sourceSet ->
			allSource + sourceSet.allSource
		}
		List sortPatterns = sortOrder.collect { Pattern.compile(it) }
		allFiles.asFileTree.matching(patterns).each { file ->
			organizeFile(file, sortPatterns)
		}
	}

	protected organizeFile(File file, List sortPatterns) {
		int importStart = -1
		int importEnd = -1

		List lines = file.readLines('UTF-8')
		Set imports = []

		lines.eachWithIndex { line, index ->
			Matcher m = IMPORT_PATTERN.matcher(line)
			if (m.find()) {
				if (importEnd < 0) {
					if (importStart < 0) {
						importStart = index
					}
					imports << new Import(qualifiedName: m[0][2], simpleName: m[0][3], staticImport: m[0][1])
				} else {
					throw new GradleException("Found unexpected import (${line}) on line ${index + 1} of ${file}")
				}
			} else if (importStart >= 0 && importEnd < 0 && !line.trim().empty) {
				importEnd = index
			}
		}

		if (importStart >= 0 && importEnd >= importEnd) {
			List preImportLines = lines[0..(importStart - 1)]
			List postImportLines = lines[importEnd..-1]

			def groupedImports = imports.findAll { item ->
				if (item.starImport || !removeUnused) {
					return true
				} else {
					Pattern usePattern = ~/(?:^|[\[\{\(<\s,@!])${item.simpleName}(?:[\.\(,\s<>\)\}\]]|$)/
					return postImportLines.find { line ->
						usePattern.matcher(line).find()
					}
				}
			}.groupBy { item ->
				sortPatterns.findResult { pattern ->
					Matcher m = pattern.matcher(item.qualifiedName)
					if (m.find()) {
						[staticImport: item.staticImport, pattern: pattern, grouping: m[0][1]]
					} else {
						null
					}
				}
			}.sort { left, right ->
				def patternIndex = { pattern -> sortPatterns.indexOf(pattern) }
				def staticCompare = left.key.staticImport <=> right.key.staticImport
				if (staticCompare == 0) {
					def result = patternIndex(left.key.pattern) <=> patternIndex(right.key.pattern)
					if (result == 0) { result = left.key.grouping <=> right.key.grouping }
					return result
				} else {
					return staticImportsFirst ? staticCompare * -1 : staticCompare
				}
			}

			file.withPrintWriter('UTF-8') { writer ->
				preImportLines.each {
					writer.println it
				}
				groupedImports.each { key, importChunk ->
					importChunk.sort { item ->
						item.qualifiedName
					}.each { item ->
						StringBuilder builder = new StringBuilder()
						builder << 'import '
						if (item.staticImport) {
							builder << 'static '
						}
						builder << item.qualifiedName
						writer.println builder
					}
					writer.println()
				}
				postImportLines.each {
					writer.println it
				}
			}
		} else {
			project.logger.info('Could not determine start and end of imports in {}', file)
		}
	}

	@Immutable
	protected static class Import {
		String qualifiedName
		String simpleName
		boolean staticImport

		boolean isStarImport() {
			return simpleName == '*'
		}
	}
}

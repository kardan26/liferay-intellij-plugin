/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.ide.idea.language.osgi;

import com.intellij.codeInspection.deadCode.UnusedDeclarationInspection;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.LanguageLevelModuleExtension;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.testFramework.IdeaTestUtil;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.PsiTestUtil;
import com.intellij.testFramework.fixtures.DefaultLightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import com.intellij.util.PathUtil;

import com.liferay.ide.idea.util.SdkUtil;

import java.io.File;

import org.jetbrains.annotations.NotNull;

import org.junit.Test;

/**
 * @author Dominik Marks
 */
public class LiferayOsgiImplicitUsageProviderTest extends LightJavaCodeInsightFixtureTestCase {

	public void ignoreTestOsgiImplicitUsage() {
		myFixture.configureByFile("MyComponent.java");
		myFixture.checkHighlighting();
	}

	@Test
	public void testOsgiComponentConstructorImplicitUsage() {
		myFixture.configureByFiles("MyServiceWrapper.java", "com/liferay/portal/kernel/service/ServiceWrapper.java");
		myFixture.checkHighlighting();
	}

	@NotNull
	@Override
	protected LightProjectDescriptor getProjectDescriptor() {
		return _javaOsgiLibDescriptor;
	}

	@Override
	protected String getTestDataPath() {
		return _TEST_DATA_PATH;
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		myFixture.enableInspections(new UnusedDeclarationInspection(true));
	}

	private static final String _TEST_DATA_PATH =
		"testdata/com/liferay/ide/idea/language/osgi/LiferayOsgiImplicitUsageProviderTest";

	private static final LightProjectDescriptor _javaOsgiLibDescriptor = new DefaultLightProjectDescriptor() {

		@Override
		public void configureModule(
			@NotNull Module module, @NotNull ModifiableRootModel modifiableRootModel,
			@NotNull ContentEntry contentEntry) {

			ProjectJdkTable.getInstance(
			).addJdk(
				SdkUtil.getTestJdk()
			);

			LanguageLevelModuleExtension languageLevelModuleExtension = modifiableRootModel.getModuleExtension(
				LanguageLevelModuleExtension.class);

			if (languageLevelModuleExtension != null) {
				languageLevelModuleExtension.setLanguageLevel(LanguageLevel.JDK_1_8);
			}

			modifiableRootModel.setSdk(SdkUtil.getTestJdk());

			File testDataDir = new File(_TEST_DATA_PATH);

			final String testDataPath = PathUtil.toSystemIndependentName(testDataDir.getAbsolutePath());

			VfsRootAccess.allowRootAccess(Disposer.newDisposable(), testDataPath);

			PsiTestUtil.addLibrary(modifiableRootModel, "OSGi", testDataPath, "osgi.jar");
		}

		@Override
		public Sdk getSdk() {
			return IdeaTestUtil.getMockJdk18();
		}

	};

}
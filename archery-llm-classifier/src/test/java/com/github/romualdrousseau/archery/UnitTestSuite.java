package com.github.romualdrousseau.archery;

import org.junit.platform.suite.api.ExcludeTags;
import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeTags("unit")
@ExcludeTags("full")
@SelectPackages("com.github.romualdrousseau.archery")
public class UnitTestSuite {
}

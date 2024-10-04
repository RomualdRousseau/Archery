package com.github.romualdrousseau.archery;

import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeTags({"unit", "full"})
@SelectPackages("com.github.romualdrousseau.archery")
public class FullTestSuite {
}

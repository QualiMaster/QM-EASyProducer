Currently we rely on the files for extracting the types at runtime in OSGi.
For obtaining the resources via the class loader we need fixed names.
Integrating Maven with OSGi builds would require Tycho. May be an option in
the future. Updating happens via build.xml.
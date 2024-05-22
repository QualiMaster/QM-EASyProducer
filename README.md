# QM-EASyProducer
The EASy-Producer extensions for the EU QualiMaster project (http://qualimaster.eu) maps selected classes from the QualiMaster infrastructure into EASy-Producer, in particular 
the Variability Instantiation Language (VIL) and its extension for runtime instantiation rt-VIL. This allows using concepts from the QualiMaster
infrastructure within these languages and to even simulate executions using the underlying implementations.

The implementation bridges the Maven nature of the QualiMaster infrastructure and the Eclipse nature of EASy-Producer. For using this extension, 
EASy-Producer (https://github.com/SSEHUB/EASyProducer) must be installed (either as source code or as release bundles). Also the QualiMaster infrastructure
configuration tool (QM-IConf, https://github.com/QualiMaster/QM-IConf) uses the EASy-Producer extensions for QualiMaster.

Further, the extension also contains a debug program useful to load a (local) version of the QualiMaster configuration model and to perform tests / debug against the source code of EASy-Producer.

In May 2024, the extensions for QualiMaster were upgraded to EASy-Producer 1.3.10 and, thereby, to JDK 17 and maven builds.

QualiMaster.Extension for EASy-Producer is released as open source under the Apache 2.0 license.
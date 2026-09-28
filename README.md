# CLAS12 CTOF Calibration — Historical Research Software

## Status

**Historical research archive / legacy scientific software**

This repository contains software and auxiliary files associated with calibration and monitoring studies of the Central Time-of-Flight (CTOF) detector developed for the CLAS12 experiment at Jefferson Lab (JLab).

The repository is preserved as a historical research artifact. It is **not** presented as production-ready software or as a currently supported CLAS12 calibration framework.

## Scientific context

The CLAS12 Central Time-of-Flight detector provides timing information used for charged-particle identification in the central region of the CLAS12 spectrometer.

The software in this repository was developed in the context of detector commissioning, monitoring, and calibration work. The original code reflects the computing environment, software dependencies, file organisation, and analysis practices used at the time of development.

## Calibration workflow

The historical analysis implements or supports several detector-calibration quantities, including:

- event-by-event histogram filling;
- timing and detector-channel monitoring;
- effective propagation velocity estimation;
- geometric-mean timing quantities;
- logarithmic timing/position observables;
- gain calibration and plateau fitting.

A representative geometric-mean quantity is constructed from signals at the two ends of a scintillator bar:

$$T_{GM} = \frac{T_1 + T_2}{2}$$

while position-sensitive timing information can be related to the signal difference through a logarithmic or time-difference observable.

The exact implementation should be interpreted together with the original source code and configuration files.

## Software components

The repository contains historical Java, C++, and numerical-analysis components, including:

- `CTOFCalibration.java` — main calibration application;
- `CTOFMonitor.java` — monitoring and histogram-analysis functionality;
- `ftofHVMainFrame.cc` — auxiliary detector/HV-related C++ code;
- `hvfit.kumac` — historical numerical fitting macro;
- detector/configuration and auxiliary data files;
- NetBeans project metadata and build files.

The Java code uses historical CLAS12/JLab analysis libraries and ROOT-related histogram/function interfaces.

## Repository structure

The original structure has been retained as far as practical to preserve provenance. In particular, the following directories/files are part of the historical software environment:

- `src/ctofcalibration/`
- `lib/`
- `nbproject/`
- `build.xml`
- `ctof-daqtest-map.xlsx`
- `hv-6390.txt`
- `hvfit.kumac`
- `manifest.mf`

Some paths and dependencies are specific to the original development environment and are therefore not expected to work unchanged on a modern system.

## Historical implementation notes

The original implementation contains hard-coded local paths and references to specific detector data files. Some methods are incomplete or environment-dependent, and parts of the original build chain rely on software libraries that are no longer bundled with the repository.

Consequently:

> The repository should be regarded as a provenance-preserving archive rather than a reproducible modern software package.

Modernisation should be performed in a separate branch or successor repository so that the historical state remains identifiable.

## Data and provenance policy

Files such as detector maps, HV settings, and auxiliary configuration files are retained only where they are already part of the public historical repository.

Before redistribution or publication of any additional experimental data, configuration, or collaboration-internal material, its provenance and distribution rights should be verified.

No claim is made here that the archived auxiliary files constitute a complete CLAS12 calibration dataset.

## Reproducibility

A complete reproduction of the original analysis may require:

1. the historical Java/JLab software environment;
2. the corresponding CLAS12 libraries;
3. compatible EVIO detector data;
4. the original detector configuration and calibration context;
5. historical operating-system and build dependencies.

A future archival effort should document the required software versions and, where legally and technically appropriate, provide a containerised or otherwise reproducible environment.

## Future archival work

Potential follow-up work includes:

- adding a historical software citation;
- documenting the original development period and detector context;
- identifying exact library versions;
- separating source code from auxiliary calibration data;
- adding a modern reproducibility layer without modifying the historical implementation;
- linking this archive to later detector-instrumentation and scientific-computing work.

## Research trajectory

This repository represents the **detector instrumentation / experimental analysis** stage of a broader research trajectory:

**Detector instrumentation → physical modelling → laser/DED process modelling → machine learning → autonomous experimental systems**

The repository is therefore retained as part of the technical provenance of later work rather than as the main active development project.

## Citation

If this repository or its historical software is used in research, please cite the repository and, where applicable, the associated CLAS12 detector/calibration publications.

## Author

Historical repository associated with work on CLAS12 CTOF detector calibration and monitoring.

---

**Archive note:** This README was added as a documentation layer without altering the historical source code.
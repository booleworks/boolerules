# PRL (Pragmatic Rule Language Compiler)

⚠ This is a developer preview, pre-alpha, and **not ready for production use** ⚠

The PRL is a small language developed by BooleWorks for documenting Boolean rule sets as often found in configuration
problems like in product or software configuration.

The focus of the PRL lies on ease of use, clear concepts and - like the name suggests - pragmatism.  In our daily job in
the German automotive industry we have to handle dozens of different rule languages with their own unique syntax and
semantic.  Our goal was a rule language, where we can model all these different languages which we know without too much
hustle.

## Description

### Different feature types

The language has four types of features:

1. *Boolean Features* for classic product options that can be included in a configuration or not (e.g., trailer hitch).
2. *Versioned Boolean features* for software packages that can not only be present or absent, but also be available in a
   specific version. This allows for elegant conditions about software versions, e.g., Version 2 of a package requires
   another package to be at least in Version 4.
3. *Enum Features* for modeling characteristics and their values. Unlike simple Boolean features that only model the
   presence of an option, a characteristic can have more than two values. This allows for the modeling of elements where
   each configuration must have exactly one value. For example, the paint of a vehicle could be modeled as a
   characteristic `paint` with the different colors as values.
4. *Integer Features* for modeling options with numerical values. For instance, options such as the number
   of cylinders or the number of megapixels of a front camera can be modeled.

```
bool feature cupholder
versioned bool feature navigation_system
int feature front_camera_mp [1, 2, 4, 8]
int feature tire_size [16 - 21]
enum feature steering ["LL", "RL"]
```


### Constraints

Using these features, constraints can be constructed with classic Boolean operators (And, Or, Not, Implies, Equivalence)
and predicates on the extended features. These predicates are:
- Comparison of feature values of Enum Features
- Set inclusion of Enum Features (whether a feature is assigned one of several values)
- Numerical comparisons of Integer Features (whether a feature has a value greater/less than another value)
- Set inclusion of Integer features
- Version comparisons of Versioned Boolean Features

```
cupholder
navigation_system[>3]
[front_camera_mp <= 4]
[front_camera_mp in [1, 2, 4]]
[front_camera_mp <= rear_camera_mp]
[steering = "LL"]
```


### Rules

 From these constraints, seven different types of rules can be constructed:

1. *If-Then Rules:* If the `if` part is true, then the `then` part must also be true, often referred to as a 
   requirement.
2. *If-Then-Not Rules:* If the `if` part is true, then the `then-not` part must not be true, often referred to as
   an exclusion.
3. *If-Then-Else Rules:* If the `if` part is true, then the `then` part must be true, otherwise the `else` part.
4. *Definitions:* A feature can be equated with a constraint, meaning whenever the feature is true, the constraint
   must also be true, and vice versa.
5. *Forced and Forbidden Features:* Certain features or expressions can be enforced or prohibited.
6. *Optional and Mandatory Groups:* Multiple Boolean features can be grouped together so that all features in the group
   are mutually exclusive. The group name itself is again a feature and can be used as such.
7. *Arbitrary Constraints* without further limitation.

```
rule if park_assistant then park_sensors_front & park_sensors_rear
rule if paint_red thenNot upholstery_blue
rule if sport_package then sport_interior else standard_interior
rule benelux is belgium / netherlands / luxemburg
rule mandatory feature xenon_light
rule forbidden feature simple_radio
optional group cupholders contains [single_cupholder, double_cupholder]
mandatory group radio contains [simple_radio, dab_radio, internet_radio]
rule [front_camera_mp >= 4] / front_radar
```


### Properties and Slicing

In addition to features and rules, the last important component of PRL is the concept of *properties and
slicing*.  This language concept also differentiates PRL from most of the existing mathematical constraint satisfaction
modeling languages, such as the SMT-Lib format or Mini-Zinc.

One of the major challenges in modeling a real product or software rule set is that these rules are often not specific
to a single point in time, a release, or a particular product series, but are defined in a more comprehensive way.
However, algorithms are frequently intended to be executed not just on one specific subset, but across, for example, a
period of time or multiple releases simultaneously. Sometimes the interest lies in the result at each individual
evaluation point, sometimes in the results over all, or only in the question of whether there is an evaluation point at
which a certain conditions holds. These three questions are illustrated by the following example:

The aim is to compute whether a certain feature is mandatory, i.e., every configuration of a product contains the
feature. With a rule set that is date-dependent and spans a long period, the following three questions might be of
interest:

1. Is the feature mandatory anywhere in the whole product lifespan? (`ANY`)
2. Is the feature mandatory in the whole product lifespan? (`ALL`)
3. At which points in time the feature is mandatory and at which points not (`SPLIT`)

The goal of PRL is to be able to annotate all features and rules with any such properties (like date, series, or
release). A single slice is then a specific section of the rule set that considers exactly one value for each property,
e.g., the rule set for a specific date, series, and release. All algorithms can thus compute on arbitrary
multi-dimensional sub-sections of the rule set and summarize them according to the three mentioned aspects: `SPLIT`,
`ANY`, and `ALL`. For this purpose, there are four different types of properties:

1. *Boolean Properties* for simple flags, such as active/not active.
2. *Enum Properties* for textual values that assume a certain value, like a series.
3. *Int Properties* for numerical values, such as the version of a rule.
4. *Date Properties* for time periods and dates.

Numerical values and date values can be specified both as a list of values and as intervals. A property can be "slicing",
meaning it can be differentiated in the computation later, or "non-slicing", in which case it is included for
information purposes, like the description of a feature or the name of the last editor of a rule. This information is
stored and compiled as meta-information with features or rules but is not used for calculation. The set of slicing
properties is specified centrally in the PRL rule file.

```
rule mandatory feature xenon_light {
  id "original-id-4711"
  description "xenon light is mandatory"
  decided 2023-01-01
  validity [2023-03-01 – 2026-12-31]
  releases ["R2023/01", "2023/07", "2023/12"]
  model_series "ms1"
}
```

## The Ecosystem

This repository contains the compiler for the PRL. It compiles a PRL test file into an internal representation and can
store it as a serialized Protocol Buffer.  If you want to see a use case for the PRL,
[BooleRules](https://github.com/booleworks/boolerules) is a rune engine which provides algorithms to compute with
such rule sets.

An IntelliJ Plugin for some syntax highlighting and refactoring functionality can be found 
[here](https://github.com/booleworks/prl-intellij-plugin).

## Funding

PRL and BooleRules development is funded by the [SofDCar project](https://sofdcar.de/):

<a href="https://www.logicng.org"><img src="https://github.com/booleworks/logicng-rs/blob/main/doc/logos/bmwk.png?raw=true" alt="logo" width="200"></a>


# BooleRules Rule Engine

<img src="https://github.com/booleworks/boolerules/blob/main/docs/boolerules-logo.png?raw=true" alt="logo" width="400"></a>

⚠ This is a developer preview, pre-alpha, and not ready for production use ⚠

BooleRules is a rule engine with its main focus on the **computation on and verification of** very large rule sets
describing extreme variant spaces like encountered e.g. in product or software configuration.  Its input language is the
[Pragmatic Rule Language (PRL)](https://github.com/booleworks/prl-compiler) and its mathematical backbone is the logic
library [LogicNG](https://www.logicng.org).

The BooleRules ecosystem is developed by [BooleWorks](https://www.booleworks.com), a German company specialised on the
application of logic for product configuration and variant management.

## Description

<img src="https://github.com/booleworks/boolerules/blob/main/docs/boolerules-architecture.png?raw=true" alt="logo" width="600"></a>

The two central compontents of BooleRules are the PRL compiler and the server component.  Redis is used as a fast
persistence layer.

<img src="https://github.com/booleworks/boolerules/blob/main/docs/boolerules-compiler.png?raw=true" alt="logo" width="500"></a>

The compiler parses a rule file in PRL and compiles it into an internal format. During this process, a variety of checks
are carried out to ensure that the rule file is consistent. In addition to standard checks for correct syntax (which are
already done in the parser), it also ensures that features are correctly defined and used correctly according to their
feature type in rules.  For example, a Boolean feature cannot be compared with a numerical value – this would lead to a
compiler error. It is also ensured that features are correctly defined across all possible slices and that, for example,
there is no slice in which there are two different definitions for a feature at the same time. This ensures that a
compiled rule file can be guaranteed to be used for computations afterward. For efficient storage, this internal
compiled data format is then serialized into a binary format using Google Protocol Buffers. 

The transpiler can translate a compiled rule file into formulas for LogicNG. Here, for example, all types of rules are
translated into corresponding propositional logic constructs, or enum features are translated into a combination of
Boolean variables and cardinality constraints. The transpiler also takes care of creating the necessary slices for a
computation and optimizes them with respect to their resulting rule set. the transpiler can merge several slices of a
rule set if an `ALL` calculation is required. This is necessary because there are computations where a computation over
all slices cannot be carried out with different computations, but the computation must be based on a consistent rule
set.

For example, if one wants to compute the heaviest configuration that can be built over the entire period, one cannot
simply compute for each period and summarize the results at the end. Here, the rule set must be combined and then
computed with a single optimization to determine the appropriate configuration.

## Funding

BooleRules development is funded by the [SofDCar project](https://sofdcar.de/):

<a href="https://sofdcar.de"><img src="https://github.com/booleworks/logicng-rs/blob/main/doc/logos/bmwk.png?raw=true" alt="logo" width="200"></a>


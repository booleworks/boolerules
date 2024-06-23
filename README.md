# BooleRules Rule Engine

<img src="https://github.com/booleworks/boolerules/blob/main/assets/boolerules-logo.png?raw=true" alt="logo" width="400"></a>

⚠ This is a developer preview, pre-alpha, and not ready for production use ⚠



BooleRules is a rule engine with its main focus on the **computation on and verification of** very large rule sets
describing extreme variant spaces like encountered e.g. in product or software configuration.  Its input language is 
the [Pragmatic Rule Language (PRL)](https://github.com/booleworks/boolerules/tree/main/prl-compiler) and its 
mathematical backbone is the logic library [LogicNG](https://www.logicng.org).

The BooleRules ecosystem is developed by [BooleWorks](https://www.booleworks.com), a German company specialised on the
application of logic for product configuration and variant management.

<img src="https://github.com/booleworks/boolerules/blob/main/assets/screenshot.png?raw=true" alt="logo" width="600">

## Components of this Repository

In this repository you find

- [prl-compiler](https://github.com/booleworks/boolerules/tree/main/prl-compiler): The definition and compiler for the 
  PRL
- [server](https://github.com/booleworks/boolerules/tree/main/server): The BooleRules web application where all 
  algorithms are implemented
- [showcase](https://github.com/booleworks/boolerules/tree/main/showcase): A small GUI for demonstrating BooleRules
  functionality
- [proto](https://github.com/booleworks/boolerules/tree/main/proto):  The ProtoBuf definitions for BooleRules
- [test-files](https://github.com/booleworks/boolerules/tree/main/test-files): Some test files
- [intellij-plugin](https://github.com/booleworks/boolerules/tree/main/intellij-plugin): An IntelliJ plugin
  for the PRL
- [tree-sitter-grammar](https://github.com/booleworks/boolerules/tree/main/tree-sitter-grammar): A treesitter grammar
  for the PRL

## Running BooleRules

The easiest way to run BooleRules is using Docker Compose (which is part of every normal Docker Desktop installation). Just open a terminal in this folder, run `docker compose up` and then open `localhost:3000` from your browser.

## Funding

BooleRules development is funded by the [SofDCar project](https://sofdcar.de/):

<img src="https://github.com/booleworks/boolerules/blob/main/assets/bmwk.png?raw=true" alt="logo" width="200">

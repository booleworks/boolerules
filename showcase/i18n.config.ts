export default defineI18nConfig(() => ({
    warnHtmlMessage: false,
    legacy: false,
    locale: 'en',
    messages: {
        en: {
            start: {
                welcome: "Welcome to",
                first: 'Upload a rule file in the upper right corner',
                second: 'Choose a computation on the left hand side',
                third: 'Have fun!',
            },
            rulefilebar: {
                upload_first: 'Please upload a rule file first',
                filename: 'PRL File',
                rules: 'Rules',
                features: 'Features',
                management: 'Rule File Management',
                btn_upload: 'Upload Rule File',
                btn_upload_new: 'Upload New Rule File',
            },
            rulefilemgmt: {
                filename: 'PRL File',
                uuid: 'UUID',
                size: 'Stored Size',
                uploaded: 'Uploaded',
                rules: 'Rules',
                features: 'Features',
                bool_features: 'Boolean Features',
                enum_features: 'Enum Features',
                int_features: 'Int Features',
                id: 'ID',
                stored: 'Stored Rule Files',
                btn_load: 'Load Selected File',
                btn_delete: 'Delete Selected File',
            },
            computation: {
                header_rulefile: 'Rule File',
                header_configuration: 'Configuration',
                header_optimization: 'Optimization',
                header_bom: 'Bill of Materials',
                header_software: 'Software Configuration',
                header_vis: 'Visualization',
                consistency: 'Consistency',
                backbone: 'Feature Buildability',
                enumeration: 'Buildable Combinations',
                counting: 'Configuration Counting',
                minmax: 'Min/Max Configurations',
                constraints: 'Constraint Buildability',
                bulk: 'Bulk Check',
                weights: 'Custom Weights',
                coverage: 'Maximum Coverage',
                reconfiguration: 'Reconfiguration',
                pos_uniqueness: 'Position Uniqueness',
                pos_dead: 'Dead Position Variants',
                pkg_buildability: 'Package Buildability',
                pkg_solving: 'Package Solving',
                pkg_optimization: 'Update Optimization',
                constraint_graph: 'Constraint Graph',
                feature_cloud: 'Feature Cloud',
            },
            common: {
                algdesc: 'Description',
                add_const: 'Additional Constraints',
                features: 'Features',
                constraint: 'Constraint',
                result_status: 'Computation Status and Download',
            },
            comp_stat: {
                time: 'Computation Time',
                slices: 'Slices',
                computations: 'Slice Computations',
                avg_time: 'Avg. Slice Computation Time',
                btn_download: 'Download Result',
                job_id: 'Job ID',
            },
            slices: {
                selection: 'Slice Selection',
                property: 'Property',
                type: 'Type',
                slice: 'Slice',
                sel: 'Selection',
                no_props: 'The rule file has no slicing properties.',
                no_file: 'Upload a rule file to see slicing properties.',
                filter: 'Filter Values',
                single_select: 'Select Value',
                from: 'From',
                to: 'To',
            },
            result: {
                header: 'Result',
                property: 'Property: ',
                empty_config: 'Empty Configuration'
            },
            upload: {
                summary: "Upload Summary",
                error: 'There was a communication problem with the server. No file could be uploaded.',
                status: 'Status',
                warnings: 'Warnings',
                errors: 'Errors',
                infos: 'Infos',
                no_upload_errors: 'No upload due to errors',
                upload_warnings: 'Upload with warnings',
                upload_success: 'Upload successful',
            },
            details: {
                header: "Details",
                desc: "Select a single slice and compute it's detailed results for this computation.",
                btn_show: "Show Details",
                btn_compute: "Compute Details",
                btn_compute_graph: "Compute Graph",
                result: "Result",
                example: "Example Configuration",
                explanation: "Conflict Explanation",
                id: "ID",
                rule: "Rule",
                description: "Description",
                feature: "Feature",
                optimum: "Optimum",
                used_weights: "Used Weights",
                covered_constraints: "Constraints covered by the Configuration",
                error: "Explanation could not be computed. Currently explanations are not supported for all types\
                of computations, e.g. for ALL or ANY slices."
            },
            algo: {
                computation: 'Computation',
                nothing_computed: 'Nothing yet computed',
                backbone: {
                    btn_compute: 'Compute Buildability',
                    desc: 'Compute the backbone of one or more slices of a rule set. This computation\
            determines for each chosen feature if it is <i>mandatory</i> in a slice\
            meaning each buildable configuration must contain it, if it is\
            <i>forbidden</i> meaning each buildable configuration cannot contain it,\
            or if it is <i>optional</i> meaning there are configuration with and\
            without it. <br />\
            <br />\
            If the list of features is left empty, the backbone will be computed for\
            all features.',
                    forbidden: 'Forbidden Feature',
                    mandatory: 'Mandatory Feature',
                    optional: 'Optional Feature',
                },
                consistency: {
                    check_details: 'Compute Details',
                    btn_compute: 'Compute Consistency',
                    desc: 'Compute the consistency of one or more slices of a rule set. A slice is\
            considered "consistent" if there is at least one buildable configuration.\
            If details are computed, an example configuration is computed for each\
            consistent slice and an explanation of the conflict is computed for each\
            inconsistent slice',
                    consistent: 'consistent',
                    inconsistent: 'inconsistent',
                },
                counting: {
                    btn_compute: 'Compute Count',
                    desc: 'Counts all buildable configurations of one or more slices of a rule set.\
            <br /><br />\
            <span class="text-red-500">Attention:</span> For large rule sets this\
            number can get very high and computation times can grow exponentially.',
                },
                enumeration: {
                    btn_compute: 'Compute Combinations',
                    desc: 'Computes all buildable configurations projected to a subset of the rule\
            set\'s features. This means you can choose which features you are\
            interested in and then all buildable combinations between these features\
            are computed.\
            <br /><br />\
            <span class="text-red-500">Attention:</span> The number of buildable\
            combinations can grow exponentially in the number of feautures you choose.',
                    combination: 'Feature Combination',
                },
                minmax: {
                    btn_compute_min: 'Compute Minimal Configuration',
                    btn_compute_max: 'Compute Maximal Configuration',
                    desc: 'Compute a configuration of minimal or maximal length. Since each int\
            and each enum feature has to be assigned a value, the optimization computed\
            the minimal/maximal number of boolean features which have to occurr in an\
            order. An example order of the respective length is also computed.',
                },
                optimization: {
                    weighting: 'Weight',
                    weightings: 'Weights',
                    btn_edit_weights: "Edit weights",
                    btn_compute_min: 'Compute Minimal Weight',
                    btn_compute_max: 'Compute Maximal Weight',
                    btn_upload_weights: "Upload CSV File",
                    loaded_weights: 'Loaded weights',
                    no_weights: 'Did not yet load any weights',
                    desc: 'You can provide own weights per constraint and then compute a configuration\
            of minimal or maximal weight.  Such weights could be e.g. prices, masses, or\
            units of work.',
                },
                coverage: {
                    btn_edit_constraints: "Edit constraints",
                    btn_compute: 'Compute Coverage',
                    btn_upload_constraints: 'Upload Constraints',
                    constraints: 'Constraints',
                    loaded_constraints: 'Loaded constraints',
                    no_constraints: 'Did not yet load any constraints',
                    desc: 'Compute the minimum number of configurations s.t. all the constraints are covered (separately or pairwise).',
                    header_required_configurations: 'Required Configurations',
                    header_uncoverable_constraints: 'Uncoverable Constraints',
                    graph_coverable_constraints: 'Coverable Constraints',
                    configurations_axis: 'Configurations',
                    coverable_constraints_axis: 'Coverable Constraints',
                },
                reconfiguration: {
                    desc: 'Fix an invalid configuration by removing and/or adding features.',
                    algorithm: 'Algorithm',
                    btn_compute: 'Compute Reconfiguration',
                    btn_edit_configuration: 'Edit Configuration',
                    btn_upload_configuration: 'Upload Configuration',
                    loaded_configuration: 'Loaded Order',
                    no_configuration: 'Did not yet load an order',
                    configuration: 'Configuration',
                    features_to_add: 'Features to Add',
                    features_to_remove: 'Features to Remove',
                }
            },
        },
        de: {
            start: {
                welcome: "Willkommen zu",
                first: 'Lade eine PRL Datei in der rechten oberen Ecke hoch',
                second: 'Wähle eine Berechnung auf der linken Seite aus',
                third: 'Auf geht\'s!',
            },
            rulefilebar: {
                upload_first: 'Lade zuerst eine PRL Datei hoch',
                filename: 'PRL Datei',
                rules: 'Regeln',
                features: 'Features',
                management: 'PRL Datei Management',
                btn_upload: 'Upload PRL',
                btn_upload_new: 'Upload Neue PRL Datei',
            },
            rulefilemgmt: {
                filename: 'PRL Datei',
                uuid: 'UUID',
                size: 'Größe',
                uploaded: 'Hochgeladen',
                rules: 'Regeln',
                features: 'Features',
                bool_features: 'Boolsche Features',
                enum_features: 'Enum Features',
                int_features: 'Int Features',
                id: 'ID',
                stored: 'Gespeicherte PRL Dateien',
                btn_load: 'Ausgewählte Datei Verwenden',
                btn_delete: 'Ausgewählte Datei Löschen',
            },
            computation: {
                header_rulefile: 'Regel Datei',
                header_configuration: 'Konfiguration',
                header_optimization: 'Optimierung',
                header_bom: 'Stückliste',
                header_software: 'Software Konfiguration',
                header_vis: 'Visualisierung',
                consistency: 'Konsistenzprüfung',
                backbone: 'Feature-Baubarkeit',
                enumeration: 'Baubare Kombinationen',
                counting: 'Anzahl Konfigurationen',
                minmax: 'Min/Max Konfigurationen',
                constraints: 'Baubarkeit von Regeln',
                bulk: 'Massenprüfung',
                weights: 'Eigene Gewichtungen',
                coverage: 'Maximale Überdeckung',
                reconfiguration: 'Rekonfiguration',
                pos_uniqueness: 'Positions-Eindeutigkeit',
                pos_dead: 'Tote Positionsvarianten',
                pkg_buildability: 'Paket Baubarkeit',
                pkg_solving: 'Löse Paket-System',
                pkg_optimization: 'Update Optimierung',
                constraint_graph: 'Regel Graph',
                feature_cloud: 'Feature Wolke',
            },
            common: {
                algdesc: 'Beschreibung',
                add_const: 'Zusätzliche Bedingungen',
                constraint: 'Constraint',
                features: 'Features',
                result_status: 'Berechnungs-Status und Download',
            },
            comp_stat: {
                time: 'Berechnungs Zeit',
                slices: 'Slices',
                computations: 'Slice Berechnungen',
                avg_time: 'Durschnittliche Zeit pro Slice',
                btn_download: 'Ergebnis Download',
                job_id: 'Job ID',
            },
            slices: {
                selection: 'Slice Auswahl',
                property: 'Eigenschaft',
                type: 'Typ',
                slice: 'Slice',
                sel: 'Auswahl',
                no_props: 'Die PRL Datei hat keine relevanten Eigenschaften',
                no_file: 'Lade eine PRL Datei hoch, damit du Slices zur Auswahl siehst.',
                filter: 'Filtere Werte',
                single_select: 'Wert auswählen',
                from: 'Von',
                to: 'Bis',
            },
            result: {
                header: 'Ergebnis',
                property: 'Eigenschaft: ',
                empty_config: 'Leere Konfiguration'
            },
            upload: {
                summary: "Upload Zusammenfassung",
                error: 'Es gab ein Kommunikationsproblem mit dem Server. Es wurde keine Datei hochgeladen',
                status: 'Status',
                warnings: 'Warnmeldungen',
                errors: 'Fehlermeldungen',
                infos: 'Infomeldungen',
                no_upload_errors: 'Kein Upload, da es Fehler gab',
                upload_warnings: 'Upload mit Warnungen',
                upload_success: 'Erfolgreicher Upload',
            },
            details: {
                header: "Details",
                desc: "Wähle eine einzelne Slice aus und berechne die Details zur aktuellen Berechnung.",
                btn_show: "Zeige Details an",
                btn_compute: "Berechne Details",
                btn_compute_graph: "Berechne Graph",
                result: "Ergebnis",
                example: "Beispielkonfiguration",
                explanation: "Konflikterklärung",
                id: "ID",
                rule: "Regel",
                description: "Beschreibung",
                feature: "Feature",
                optimum: "Optimum",
                used_weights: "Ausgewählte Gewichtungen",
                covered_constraints: "Von der Konfiguration erfüllte Bedingungen",
                error: "Erklärung konnte nicht berechnet werden. Aktuell werden Erklärungen nicht für jede\
                Berechnungsart unterstützt, z.B. für ANY oder ALL-Slices."
            },
            algo: {
                computation: 'Berechnung',
                nothing_computed: 'Noch nichts berechnet',
                backbone: {
                    btn_compute: 'Berechne Baubarkeit',
                    desc: 'Berechne für jede Slice eines Regelwerks, welche Features baubar sind.\
            Es gibt drei Ergebnistypen: Ein Feature kann <i>zwingend</i> sein, dann muss\
            es in jeder baubaren Konfiguration enthalten sein. Es kann <i>verboten</i>\
            sein, dann kann es in keiner baubaren Konfiguration enthalten sein. Oder es\
            kann <i>optional</i> sein, dann gibt es sowohl Konfiguration mit als auch\
            ohne diesem Feature.<br />\
            <br />\
            Die Berechnung kann für eine gegebene Liste von Features ausgeführt werden\
            oder alle Features, wenn die Liste leer gelassen wird',
                    forbidden: 'Verbotenes Feature',
                    mandatory: 'Zwingendes Feature',
                    optional: 'Optionales Feature',
                },
                consistency: {
                    check_details: 'Berechne Details',
                    btn_compute: 'Berechne Konsistenz',
                    desc: 'Berechne die Konsistenz eines Regelwerks. Ein Regelwerk ist konsistenz\
            für eine Slice, wenn es in dieser Slice mindestens eine baubare Konfiguration\
            gibt. Gibt es in einer Slice keine baubare Konfiguration, ist das Regelwerk\
            inkonsistent. Werden Details mitberechnet, so wird für jede konsistente Slice\
            eine Beispielkonfiguration berechnet und für jede inkonsistente Slice eine\
            Erklärung des Konflikts.',
                    consistent: 'konsistent',
                    inconsistent: 'inkonsistent',
                },
                counting: {
                    btn_compute: 'Berechne Anzahlen',
                    desc: 'Berechne die Anzahl aller baubaren Konfigurationen pro Slice.\
            <br /><br />\
            <span class="text-red-500">Achtung:</span> Die Anzahl baubarer Konfigurationen\
            kann exponentiell groß werden - für große Regelwerke kann die Berechnung sehr\
            lange dauern oder nicht mehr berechnet werden können.',
                },
                enumeration: {
                    btn_compute: 'Berechne Kombinationen',
                    desc: 'Berechne alle baubaren Kombinationen zwischen einer gegebenen Liste\
            von Features. <br /><br />\
            <span class="text-red-500">Achtung:</span> Die Anzahl der baubaren Kombinationen\
            kann exponentiell groß werden.',
                    combination: 'Feature Kombination',
                },
                minmax: {
                    btn_compute_min: 'Berechne Minimale Konfiguration',
                    btn_compute_max: 'Berechne Maximale Konfiguration',
                    desc: 'Berechne eine Konfiguration minimaler oder maximaler Länge pro Slice.\
            Jedes Enum und Int Feature muss immer belegt sein, über Boolesche Features\
            wird optimiert.',
                },
                optimization: {
                    weighting: 'Gewichtung',
                    weightings: 'Gewichtungen',
                    btn_edit_weights: "Bearbeite Gewichtungen",
                    btn_compute_min: 'Berechne Minimale Gewichtung',
                    btn_compute_max: 'Berechne Maximale Gewichtung',
                    btn_upload_weights: "Lade CSV Datei hoch",
                    loaded_weights: 'Geladene Gewichtungen',
                    no_weights: 'Noch keine Gewichtungen geladen',
                    desc: 'Eigene Gewichtungen können pro Constraint angegeben werden und damit\
            eine Konfiguration mit minimaler oder maximaler Gewichtung berechnet werden.\
            Eine solche Gewichtung könnten z.B. Preise, Gewichte oder Arbeitsstunden sein.',
                },
                coverage: {
                    btn_edit_constraints: "Bearbeite Constraints",
                    btn_compute: 'Berechne Überdeckung',
                    btn_upload_constraints: 'Lade CSV Datei hoch',
                    constraints: 'Constraints',
                    loaded_constraints: 'Geladene Constraints',
                    no_constraints: 'Noch keine Constraints geladen',
                    desc: 'Berechne die minimale Anzahl an Konfigurationen, sodass alle Bedingungen (einzeln oder paarweise) abgedeckt sind.',
                    header_required_configurations: 'Benötigte Konfigurationen',
                    header_uncoverable_constraints: 'Nicht baubare Bedingungen',
                    graph_coverable_constraints: 'Überdeckbare Bedingungen',
                    configurations_axis: 'Konfigurationen',
                    coverable_constraints_axis: 'Überdeckbare Bedingungen',
                },
                reconfiguration: {
                    desc: 'Repariere eine nicht baubare Konfiguration durch Entfernen und Hinzufügen von Features.',
                    algorithm: 'Algorithmus',
                    btn_compute: 'Berechne Rekonfiguration',
                    btn_edit_configuration: 'Bearbeite Konfiguration',
                    btn_upload_configuration: 'Lade CSV Datei hoch',
                    loaded_configuration: 'Geladene Konfiguration',
                    no_configuration: 'Noch keine Konfiguration geladen',
                    configuration: 'Konfiguration',
                    features_to_add: 'Hinzuzufügende Features',
                    features_to_remove: 'Zu entfernende Features',
                }
            },
        }
    }
}))

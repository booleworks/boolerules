# BooleRules Tree-sitter Grammar and Syntax Highlighting

## Installation in Neovim with the `nvim-treesitter` Plugin

### Configure the Parser in you Neovim configuration

Add the following lines to your `init.lua`

```lua
local parser_config = require("nvim-treesitter.parsers").get_parser_configs()
parser_config.prl = {
	install_info = {
		url = "path_to/tree-sitter-boolerules",
		files = { "src/parser.c" },
	},
	filetype = "prl",
}
```

### Add Filetype Detection

If you want to auto-detect `.prl` files and set their filetype to `prl` add the following autocommand:

```vim
vim.cmd([[autocmd BufNewFile,BufRead *.prl set filetype=prl]])
```

### Add Syntax Highlighting

For syntax highlighting to work, you have to manually copy the
`tree-sitter-boolerules/queries/highlights.scm` to the Tree-sitter
query directory.  Depending on your package manager the location
could be e.g.
`.local/share/nvim/site/pack/packer/start/nvim-treesitter/queries/`.
Create a `prl` folder there and copy the `highlights.scm` file there.

### Install the Parser

Finally install the parser in Neovim with `:TSInstall prl`

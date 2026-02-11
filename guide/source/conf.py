# Configuration file for the Sphinx documentation builder.
#
# For the full list of built-in configuration values, see the documentation:
# https://www.sphinx-doc.org/en/master/usage/configuration.html

# -- Project information -----------------------------------------------------
# https://www.sphinx-doc.org/en/master/usage/configuration.html#project-information

latex_use_latex_multicolumn = True

project = 'Руководство по шардингу'
copyright = '2025, Red Soft'
author = 'Red Soft'

# General configuration

import re
import os
import sys
import sphinx_rtd_theme


# https://www.sphinx-doc.org/en/master/usage/configuration.html#general-configuration

extensions = ['sphinx_design', 'sphinx_rtd_theme', 'sphinx_copybutton']

#f = open('defs.tex.txt', 'r+');
#PREAMBLE = f.read();

smartquotes = False

templates_path = ['_templates']
exclude_patterns = []

language = 'ru'

latex_additional_files = ["defs.sty"]

latex_engine = 'pdflatex'

latex_toplevel_sectioning = 'section'

latex_documents = [
 ('index', 'ShardingSphere_Guide.tex', u'Ред Шардинг', u'YourName', 'article'),
]

latex_elements = {
'passoptionstopackages' : r'''
    \PassOptionsToPackage{pdftex}{graphicx}
    \PassOptionsToPackage{numbered}{bookmark}
    \PassOptionsToPackage{tikz}{bclogo}
    ''',
'fontenc' : r'''
    \usepackage[T2A]{fontenc}
    ''',
'fontsubstitution' : r'',
'inputenc' : r'\usepackage[utf8]{inputenc}',
'preamble': r"""
\usepackage{defs}
""",
'hyperref' : r'''
\usepackage[colorlinks=true,linkcolor=blue]{hyperref}
''',
'maketitle': r"""
\nonstopmode

\thispagestyle{empty}
\begin{titlepage}
\renewcommand{\maketitle}{ O{\ } O{\ } m }{
\fancyhf{}
\thispagestyle{empty}

\topskip0pt
\vspace*{\fill}

\begin{flushright}
\Huge {\xhrulefill{red}{2mm}\color{red} Ред} Шардинг\\
\huge Руководство по шардингу\\
\Large (Ред База Данных + ShardingSphere-Proxy)\\


\end{flushright}

\vspace*{\fill}}
\end{titlepage}
""",
'tableofcontents' : r"""
\addtocounter{page}{1}

\definecolor{MidnightBlue}{RGB}{25, 25, 112}

\titleformat{\section}[display]
{\filcenter\LARGE\bfseries\color{MidnightBlue}}
{\raggedright\normalfont\Large Глава \thesection}{3pt}{}

\titleformat{\subsection}
{\filright\Large\bfseries\color{MidnightBlue}}
{\thesubsection}{10pt}{}

\titleformat{\subsubsection}
{\filright\large\bfseries\color{MidnightBlue}}
{\thesubsubsection}{10pt}{}

\titleformat{\paragraph}
{\filright\normalsize\bfseries\color{MidnightBlue}}
{\theparagraph}{1em}{}

\titleformat{\subparagraph}
{\filright\normalsize\bfseries\color{MidnightBlue}}
{\thesubparagraph}{1em}{}

\titlespacing*{\subparagraph}
  {0pt}  
  {3.25ex plus 1ex minus .2ex}
  {1em}

\renewcommand{\thetable}{\thesection.\arabic{table}}
\renewcommand{\thefigure}{\thesection.\arabic{figure}}


\makeatletter                        
\renewcommand{\sectionmark}[1]{%
  \ifnum\value{section}=0
    \markright{#1}%
  \else
    \markright{\thesection~~~#1}%
  \fi
}
\makeatother

\makeatletter
\def\PY@tok@err#1{\textcolor{black}{#1}}%
\makeatother

\makeatletter
\fancypagestyle{normal}{
\pagestyle{fancy}
\fancyhf{}
\fancyhead[R]{Руководство по шардингу\\\rightmark}
\fancyfoot[C]{\xhrulefill{red}{2mm} Стр. \thepage}
\renewcommand{\headrulewidth}{0.5pt}
}
\makeatother

\setcounter{tocdepth}{10}
\setlength{\headheight}{24pt}
\renewcommand\contentsname{Содержание}
\tableofcontents
""",
'figure_align': 'H',
}

latex_table_style = []

numfig = True  #  чтобы :numref: не игнорировался
highlight_language = 'none'  # подсветка синтаксиса в код-блоках по умолчанию выключена


#latex_show_urls = 'footnote'
# -- Options for HTML output -------------------------------------------------
# https://www.sphinx-doc.org/en/master/usage/configuration.html#options-for-html-output

html_theme = 'sphinx_rtd_theme'
html_theme_path = [sphinx_rtd_theme.get_html_theme_path()]
html_static_path = ['_static']
html_css_files = ['css/custom.css']
html_show_sourcelink = False
html_js_files = [
    'custom.js',
]

rst_prolog = """
.. |GostPassword| replace:: ``GostPassword``
.. |nbackup| replace:: ``nbackup``
.. |USE_NBACKUP_UTILITY| replace:: ``USE_NBACKUP_UTILITY``
.. |securityversion| replace:: ``security5.fdb``
.. |rdbversion| replace:: 5.0
.. role:: raw-latex(raw)
   :format: latex
"""

numfig_format = {
        'code-block': 'Листинг %s'
    }


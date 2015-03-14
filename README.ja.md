MR^3
===

## 概要
MR3 (Meta-Model Management based on RDFs Revision Reflection) は，次世代Webの候補の1つであるセマンティックWebにおけるRDF (Resource Description Framework)及びRDFS (RDF Schema)の視覚的な編集とそれらの間の関係を管理する機能を持つエディタです．

セマンティックWebを実現するための基盤技術として，計算機が理解可能なメタデータを記述するためのフレームワークであるRDFやオントロジー記述言語RDFS，OWL (Web Ontology Language)の 標準化がW3Cにより行われています．RDFはURIで表現されるリソース相互の関係を主語，述語，目的語のトリプルにより表現するためのデータモデルと 構文を提供しています．また，RDFSではリソースのタイプ (クラス)及びリソース間の関係 (プロパティ)を定義するための語彙 (オントロジー)を提供しています．計算機がメタデータをより明確に理解・推論するためには，RDFSやOWLなどのオントロジー記述言語を用いてクラス 及びプロパティを定義し，それらを用いてインスタンス (RDF)を記述する必要があります．

上記のように，RDFとRDFSはインスタンス (モデル)とオントロジー (モデルの構成要素を定義するメタモデル)という意味的に異なる記述を行います．しかし，RDFSはRDFデータモデル及び構文を用いて記述されるため， RDFとRDFSが混在するコンテンツを扱う際には，両者を区別することはユーザの負担となります．特に，特定の目的のためのメタデータを記述するため に，RDFSとRDFをユーザが定義する初期段階においては，両者の間を双方向に頻繁に編集するため，RDFSとRDFを分離し，両者の整合性を保ちなが ら記述を支援することが重要となります．

MR3はRDFSとRDFを分離し，両者の間の整合性を保ちながら視覚的に編集する機能を提供することでユーザの負担を軽減します．

## 特徴
MR3には主に以下の3つの特徴があります．

* RDF (インスタンス)の視覚的編集機能
  * RDFデータモデルに基づき，主語，述語，目的語のトリプルを視覚的に編集する機能
* RDFS (オントロジー)の視覚的編集機能
	* RDFSモデルに基づき，クラス及びプロパティの上位・下位関係，プロパティの定義域及び値域の視覚的編集機能
* メタモデル管理機能
	* RDFとRDFSの間の整合性を管理し，変更を双方向に反映させる機能

上記の機能の詳細は以下のようになっています．

* RDFファイルをインポートし，RDFデータモデルを視覚的に表示・編集する機能
* RDFデータグラフを様々なRDF構文 (RDF/XML, N-Triple, N3)に基づいてファイルにエクスポートする機能
* RDFSファイルをインポートし，RDFSデータモデルを視覚的に表示・編集する機能
* RDFSデータグラフを様々なRDF構文 (RDF/XML, N-Triple, N3)に基づいてRDFSファイルにエクスポートする機能
* RDFリソースタイプの変更をRDFSクラス及びRDFSプロパティの定義域及び値域に反映させる機能
* RDFプロパティの変更をRDFSプロパティに反映させる機能
* RDFSクラス及びプロパティの変更をRDFリソースのタイプ及びプロパティに反映させる機能

## ライセンス
MR3はフリーソフトウェアです．Free Software Foundation による [GNU Generic Public License](http://www.gnu.org/copyleft/gpl.html) のバージョン2 （または，それ以降のバージョン）に従う限り自由に変更し再配布することができます．MR3は，セマンティックWebコンテンツ（RDF，RDFS，OWL，N-Tripleなど）を構築するために[Jena 2 Semantic Web Toolkit](http://jena.sourceforge.net/)を利用しています．Jena2のライセンスについては，以下のリンク（[Jena – License and Copyright](http://jena.sourceforge.net/license.html)）を参照してください．MR3は，グラフのレイアウトを行うためにVGJ（[GPL ライセンス](http://www.eng.auburn.edu/department/cse/research/graph_drawing/COPYING)）または，JGraphpad（[LGPL ライセンス](http://www.jgraph.com/license.html)）のコードを利用しています．
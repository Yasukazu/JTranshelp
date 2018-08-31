# JTranshelp
A translation helper program for English to Japanese translation implemented into Java. : 英和翻訳を助けるJavaプログラムです。
## How to use : 使い方
 1. Put 'direct-translated' sentence into a text file named 'input.txt'. : 英語を日本語に直訳した文を「input.txt」に入れて，
 2. Execute 'Main.java' as a Java program. : 'Main.java' をJavaプログラムとして実行します。 > javac Main.java ; java Main
 3. Input format: 直訳の形式 : 式 -> 変換された後の形
  - Enclosure　for priority: 優先順位を作るためにカッコに入れる:　a / [ b c ] -> [b c ] a
  - Reverse: 前後を入れ替える : A / B -> B A　; スラッシュの前後には空白が必要
  - Isolate sub sentences: 句を独立させる: A / B , C / D -> B A , D C ; カンマの前後には空白が必要
  4. Non-printable characters : 表示されない文字
   - Operators: /　: 変換子
   - Brackets: [ ] : 角括弧 

 ## Not yet implemented : まだ実装していないもの
  - Isolate sub sentences (,) : コンマによる句の分離

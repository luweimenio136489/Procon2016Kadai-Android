package mksource;

import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXTextField;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.io.*;
import java.math.BigInteger;
import java.net.URL;
import java.util.*;


public class Controller implements Initializable {
    @FXML
    private Pane mainpage;
    @FXML
    private JFXListView fileList;
    @FXML
    private Label currentDir;
    @FXML
    private JFXTextField outputname;
    @FXML
    private Label geneLog;

    public static String SOUTAI_FILE = "abs";
    public static String STYLE_HEADING1 = "head1";
    public static String STYLE_HEADING2 = "head2";
    public static String STYLE_HEADING3 = "head3";

    public void buttonsHandler(ActionEvent event) {
//        System.out.println(((Button) event.getSource()).getId());
        switch (((Button) event.getSource()).getId()) {
            case "picker"://ファイル取得
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Open Resource File");
                File userDirectory = new File("." + File.separator + SOUTAI_FILE);
                fileChooser.setInitialDirectory(userDirectory);

                fileChooser.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter("All Files", "*.*"));
                List<File> selectedFile = fileChooser.showOpenMultipleDialog(mainpage.getScene().getWindow());
                if (selectedFile != null) {
                    for (int i = 0; i < selectedFile.size(); i++) {
                        if (isTextFile(selectedFile.get(i).getAbsolutePath()))
                            fileList.getItems().add(new Label(selectedFile.get(i).getAbsolutePath()));

                    }
                }
                break;
            case "clear"://一覧削除
                while (!fileList.getItems().isEmpty()) fileList.getItems().remove(0);
                break;
            case "generate":
                List<String> pathList = new ArrayList<>();
                fileList.getItems().forEach(s -> pathList.add(((Label) s).getText()));
                createDocument(pathList);
                break;
        }

    }

    public static boolean isTextFile(String filePath) {

        FileInputStream in = null;
        try {
            in = new FileInputStream(filePath);

            byte[] b = new byte[1];
            while (in.read(b, 0, 1) > 0) {
                if (b[0] == 0) {
                    return false;
                }
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                in = null;
            }
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String path = new File(".").getAbsoluteFile().getParent();
        File abs = new File(path + "/" + SOUTAI_FILE);
        if (!abs.exists()) abs.mkdir();
        currentDir.setText("\"" + path + "\\" + SOUTAI_FILE + "\"以下にあるファイルは相対パスで記述されます");
    }


    public void createDocument(List<String> filePath) {
        final String fileName = outputname.getText();//file-name読み込み
        List<List<String>> extArray = new ArrayList<>();//拡張子含めたリスト
        HashSet<String> extension = new HashSet<>();//拡張子判別
        XWPFDocument document = null;
        XWPFTable table;
        XWPFParagraph paragraph;
        XWPFRun run;
        XWPFStyles styles = null;
        FileOutputStream outputStream = null;

        //拡張子でソートしたほうがいいかと思ったが、よく考えたらファイルパスベースだからいらないか。
//        filePath.forEach(s -> extension.add(getExtension(s)));//拡張子リスト作成
//        extension.forEach(s -> extArray.add(new ArrayList<>()));

        //一応パス順にsort
        Collections.sort(filePath);

        String prebFolder = " ";

        try {
            document = new XWPFDocument();
            styles = document.createStyles();
            addCustomHeadingStyle(document, styles, STYLE_HEADING1, 1, 24, "000000");
            addCustomHeadingStyle(document, styles, STYLE_HEADING2, 2, 20, "000000");
            addCustomHeadingStyle(document, styles, STYLE_HEADING3, 3, 12, "000000");

            int headCount1 = 1;
            int headCount2 = 1;
            //ここからドキュメント作成
            for (String path : filePath) {

                if (!prebFolder.equals(getRelativeFilePath(path))) {//前と相対パスが異なったら
                    paragraph = document.createParagraph();//タイトルづくり
                    paragraph.setStyle(STYLE_HEADING1);
                    run = paragraph.createRun();
                    run.setFontFamily("MS ゴシック");
                    prebFolder = getRelativeFilePath(path);
                    run.setText(headCount1 + " " + prebFolder);
                    headCount1++;
                    headCount2 = 1;
                }
                if (!new File(path).exists()) continue;//error handling

                paragraph = document.createParagraph();//ファイル名
                paragraph.setStyle(STYLE_HEADING2);
                run = paragraph.createRun();
                run.setFontFamily("MS ゴシック");
                prebFolder = getRelativeFilePath(path);
                run.setText(headCount1 - 1 + "." + headCount2 + " " + new File(path).getName());


                table = document.createTable(1, 1);//資格作ってその中にテキスト入れる
                widthCellsAcrossRow(table, 0, 0, 10000);//ドキュメントのいっぱいまで引き延ばす
                paragraph = table.getRow(0).getCell(0).getParagraphs().get(0);
                paragraph.setAlignment(ParagraphAlignment.LEFT);
//                paragraph.setStyle(STYLE_HEADING3);
                run = paragraph.createRun();
                for (String ss : readTXTFile(path).split("\n")) {
                    run.setFontSize(10);
                    run.setFontFamily("MS 明朝");
                    run.setText(ss);
                    run.addBreak();
                }


                headCount2++;
                paragraph = document.createParagraph();//ファイル名
                run = paragraph.createRun();
                run.addCarriageReturn();


            }
            outputStream = new FileOutputStream(fileName);
            document.write(outputStream);

            geneLog.setText("作成完了！");
        } catch (FileNotFoundException e) {
            geneLog.setText("FileNotFoundException");
            e.printStackTrace();
        } catch (IOException e) {

            geneLog.setText("IOException");
            e.printStackTrace();
        }

    }

    public String getRelativeFilePath(String filePath) {
        if (!new File(filePath).exists()) return "存在しないよ";
        return filePath.replace(new File(".").getAbsoluteFile().getParent() + File.separator + SOUTAI_FILE, "..").replace(new File(filePath).getName(), "");
    }

    public String getExtension(String filePath) {
        if (!new File(filePath).exists()) return "存在しないよ";
        String[] splitName = new File(filePath).getName().split(".");
        if (splitName.length == 1) return "拡張しない";
        return splitName[splitName.length - 1];
    }

    /**
     * ソースコードデータ読み込み
     * TODO:文字コード動的対応
     *
     * @param filePath
     * @return
     */
    public String readTXTFile(String filePath) {
        StringBuilder res = new StringBuilder();
        try {
            File file = new File(filePath);
            if (!file.exists()) return null;

            FileCharDetector fd = new FileCharDetector(filePath);
            BufferedReader br;
            if (fd.detector() != null) {
                br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filePath)), fd.detector()));
            } else {
                br = new BufferedReader(new FileReader(file));
            }
            String str;
            while ((str = br.readLine()) != null) {
                res.append(str + "\n");
            }

            br.close();
        } catch (FileNotFoundException e) {
            System.out.println(e);
        } catch (IOException e) {
            System.out.println(e);
        }
        return res.toString();
    }


    public static boolean isUTF8(byte[] src) {
        try {
            byte[] tmp = new String(src, "UTF8").getBytes("UTF8");
            return Arrays.equals(tmp, src);
        } catch (UnsupportedEncodingException e) {
            return false;
        }
    }

    public static boolean isSJIS(byte[] src) {
        try {
            byte[] tmp = new String(src, "Shift_JIS").getBytes("Shift_JIS");
            return Arrays.equals(tmp, src);
        } catch (UnsupportedEncodingException e) {
            return false;
        }
    }

    /**
     * どうやらテーマを自作しないといけないらしい
     * 参考：
     * *http://stackoverflow.com/questions/28628815/how-to-set-heading-style-for-a-paragraph-in-a-docx-file-using-apache-poi
     * *http://stackoverflow.com/questions/2643822/how-can-i-use-predefined-formats-in-docx-with-poi
     *
     * @param docxDocument documentのインスタンス
     * @param styles       stylesのインスタンス
     * @param strStyleId   スタイル名
     * @param headingLevel ヘッディングレベル
     * @param pointSize    文字サイズ
     * @param hexColor     色#000000~#FFFFFF
     */
    private static void addCustomHeadingStyle(XWPFDocument docxDocument, XWPFStyles styles, String strStyleId, int headingLevel, int pointSize, String hexColor) {
        CTStyle ctStyle = CTStyle.Factory.newInstance();
        ctStyle.setStyleId(strStyleId);

        CTString styleName = CTString.Factory.newInstance();
        styleName.setVal(strStyleId);
        ctStyle.setName(styleName);

        CTDecimalNumber indentNumber = CTDecimalNumber.Factory.newInstance();
        indentNumber.setVal(BigInteger.valueOf(headingLevel));

        // 若い数字にしていること目立つようになります
        ctStyle.setUiPriority(indentNumber);

        CTOnOff onoffnull = CTOnOff.Factory.newInstance();
        ctStyle.setUnhideWhenUsed(onoffnull);

        // これをすることでフォーマットのバーにスタイル名がみえる
        ctStyle.setQFormat(onoffnull);

        // 与えられたレベルのスタイルの定義
        CTPPr ppr = CTPPr.Factory.newInstance();
        ppr.setOutlineLvl(indentNumber);
        ctStyle.setPPr(ppr);

        XWPFStyle style = new XWPFStyle(ctStyle);

        CTHpsMeasure size = CTHpsMeasure.Factory.newInstance();
        size.setVal(new BigInteger(String.valueOf(pointSize)));
        CTHpsMeasure size2 = CTHpsMeasure.Factory.newInstance();
        size2.setVal(new BigInteger("24"));//24ポイント

        CTFonts fonts = CTFonts.Factory.newInstance();
        fonts.setAscii("Loma");

        CTRPr rpr = CTRPr.Factory.newInstance();
        rpr.setRFonts(fonts);
        rpr.setSz(size);
        rpr.setSzCs(size2);

        CTColor color = CTColor.Factory.newInstance();
        color.setVal(hexToBytes(hexColor));
        rpr.setColor(color);
        style.getCTStyle().setRPr(rpr);

        style.setType(STStyleType.PARAGRAPH);
        styles.addStyle(style);

    }

    public static byte[] hexToBytes(String hexString) {
        HexBinaryAdapter adapter = new HexBinaryAdapter();
        byte[] bytes = adapter.unmarshal(hexString);
        return bytes;
    }

    /**
     * 横サイズ調整
     * 参考
     * *http://stackoverflow.com/questions/20045921/set-columnwidth-of-a-table-in-xwpftablecell-docx
     *
     * @param table
     * @param rowNum
     * @param colNum
     * @param width
     */
    private static void widthCellsAcrossRow(XWPFTable table, int rowNum, int colNum, int width) {
        XWPFTableCell cell = table.getRow(rowNum).getCell(colNum);
        if (cell.getCTTc().getTcPr() == null)
            cell.getCTTc().addNewTcPr();
        if (cell.getCTTc().getTcPr().getTcW() == null)
            cell.getCTTc().getTcPr().addNewTcW();
        cell.getCTTc().getTcPr().getTcW().setW(BigInteger.valueOf((long) width));
    }
}

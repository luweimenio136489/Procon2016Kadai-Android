using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using DocumentFormat.OpenXml.Packaging;
using DocumentFormat.OpenXml;
using DocumentFormat.OpenXml.Wordprocessing;

namespace MakeSouceList
{
    class OpenXMLManager
    {

        public OpenXMLManager() { }

        public string Savefile { set; get; }
        public string[] Readfile { set; get; }

        public int flag = 0;
        public int cn = 0;
        public static HashSet<string> globalList = new HashSet<string>();

        public void MakeSouceListRun()
        {
            using (var package = WordprocessingDocument.Open(this.Savefile, true))
            {

                List<string> tmpList = new List<string>();
                var document = package.MainDocumentPart.Document;
                if (document.Elements<Body>().Count() >= 1)
                {
                    foreach (var i in GetFileCatList())
                    {
                        //データが追加されたかどうかを取っているが、実際は使われない。
                        if (i != null && globalList.Add(i))//新しくデータが追加されたら
                        {
                            tmpList.Add(i);
                        }
                     }
                    var fbodys = document.Elements<Body>();
                    var fbody = fbodys.ElementAt(0);
                    cn++;
                    this.SetUpText(cn + "." + tmpList[0] + "ファイル", fbody, "1");
                    fbody.Append(new Paragraph());
                    document.Body = fbody;
                    for (int i = 1; i < tmpList.Count; i++)
                    {
                        var body = new Body();
                        cn++;
                        this.SetUpText(cn + "." + tmpList[i] + "ファイル", body, "1");
                        body.Append(new Paragraph());
                        document.Append(body);
                    }
                    tmpList.Clear();

                }
                var bodys = document.Elements<Body>();

                for (int i = 0; i < Readfile.Length; i++)
                {

                    string filename = Readfile[i];
                    var splintfilename = filename.Split('.');
                    Body targetbody = null;
                    this.flag = 0;
                    int content = 0;
                    foreach (var el in globalList)
                    {
                        this.flag++;
                        if (splintfilename[splintfilename.Length - 1] == el)
                        {
                            content = flag;
                            targetbody = bodys.ElementAt(flag - 1);
                        }
                    }


                    var filenamesprlint = filename.Split('\\');
                    if (targetbody == null)
                        return;

                    this.SetUpText2(content, filenamesprlint[filenamesprlint.Length - 1], targetbody, "2");
                    Wordtable wt = new Wordtable();
                    targetbody.Append(wt.SAMFiledatatable(filename));
                    targetbody.Append(new Paragraph());

                }
            }
        }

        private void SetUpText(object p, Body body, string v)
        {
            throw new NotImplementedException();
        }

        private HashSet<string> GetFileCatList()
        {
            HashSet<string> hsList = new HashSet<string>();

            for (int i = 0; i < Readfile.Length; i++)
            {
                string filename = Readfile[i];
                var splintfilename = filename.Split('.');
                hsList.Add(splintfilename[splintfilename.Length - 1]);
            }
            return hsList;
        }

        private void SetUpText(string text, Body body, String ID)
        {
            Paragraph para = new Paragraph();
            Run run = new Run();
            ParagraphProperties pPr = new ParagraphProperties() { ParagraphStyleId = new ParagraphStyleId() { Val = ID } };
            run.Append(new Text(text));
            para.Append(pPr);
            para.Append(run);
            body.Append(para);
        }

        private void SetUpText2(int flag, string text, Body body, String ID)
        {
            var oldparas = body.Elements<Table>();


            Paragraph para = new Paragraph();
            Run run = new Run();
            ParagraphProperties pPr = new ParagraphProperties() { ParagraphStyleId = new ParagraphStyleId() { Val = ID } };
            run.Append(new Text(flag.ToString() + "." + (oldparas.Count() + 1).ToString() + " " + text) { Space = SpaceProcessingModeValues.Preserve });
            para.Append(pPr);
            para.Append(run);
            body.Append(para);
        }

    }
}

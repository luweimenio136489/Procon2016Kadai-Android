using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using DocumentFormat.OpenXml;
using DocumentFormat.OpenXml.Packaging;
using DocumentFormat.OpenXml.Wordprocessing;

namespace MakeSouceList
{
    class Wordtable
    {
        public Wordtable() { }

        public Table SAMFiledatatable(string filename)
        {
            FileRead file = new FileRead();
            file.filename = filename;
            Table table = new Table();
            TableProperties tableProp = new TableProperties(new TableBorders(
                    new TopBorder
                    {
                        Val = new EnumValue<BorderValues>(BorderValues.Single),
                        Size = 5
                    },
                    new BottomBorder
                    {
                        Val = new EnumValue<BorderValues>(BorderValues.Single),
                        Size = 5
                    },
                    new LeftBorder
                    {
                        Val = new EnumValue<BorderValues>(BorderValues.Single),
                        Size = 5
                    },
                    new RightBorder
                    {
                        Val = new EnumValue<BorderValues>(BorderValues.Single),
                        Size = 5
                    },
                    new InsideHorizontalBorder
                    {
                        Val = new EnumValue<BorderValues>(BorderValues.Single),
                        Size = 5
                    },
                    new InsideVerticalBorder
                    {
                        Val = new EnumValue<BorderValues>(BorderValues.Single),
                        Size = 5
                    }));
            TableStyle tableStyle = new TableStyle() { Val = "TableGrid" };

            TableWidth tablewidth = new TableWidth() { Width = "5000", Type = TableWidthUnitValues.Pct };

            tableProp.Append(tableStyle, tablewidth);
            table.AppendChild(tableProp);

            TableGrid tg = new TableGrid(new GridColumn(), new GridColumn(), new GridColumn());
            table.AppendChild(tg);

            // Create 1 row to the table.
            TableRow tr1 = new TableRow();

            // Add a cell to each column in the row.
            string[] code = file.readfile();
            TableCell tc1 = new TableCell();

            for (int i = 0; i < code.Length; i++)
            {
                Paragraph tcpara_ = new Paragraph();
                Run tcrun_ = new Run();
                tcrun_.Append(new Justification() { Val = JustificationValues.Left });
                Text codetext = new Text() { Space = SpaceProcessingModeValues.Preserve };


                var stringArray = code[i].Split('\t');
                for (int j = 0; j < stringArray.Length - 1; j++)
                    tcrun_.Append(new TabChar());

                codetext.Text = code[i];
                tcrun_.Append(codetext);
                tcpara_.Append(tcrun_);
                tc1.Append(tcpara_);
            }

            tr1.Append(tc1);

            table.AppendChild(tr1);
            return table;
        }

    }
}

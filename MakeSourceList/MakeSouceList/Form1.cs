using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;

namespace MakeSouceList
{
    public partial class Form1 : Form
    {
        private string sevefilename;

        public Form1()
        {
            InitializeComponent();
        }

        private void button1_Click(object sender, EventArgs e)
        {
            var ofd = new OpenFileDialog();
            ofd.FileName = "読み込むソースを選択してください";
            ofd.Filter = "全てのファイル(*.*)|*.*";
            ofd.Title = "開くファイルを選択してください";
            ofd.RestoreDirectory = true;
            ofd.Multiselect = true;
            ofd.ShowDialog();

            OpenXMLManager open = new OpenXMLManager() { Savefile = this.sevefilename ,Readfile = ofd.FileNames};
            open.MakeSouceListRun();
            button1.Enabled = false;
            label2.Text = "作成できたよ";
        }

        private void button2_Click(object sender, EventArgs e)
        {
            var ofd = new OpenFileDialog();
            ofd.FileName = "ソースを保存するドキュメントを選択してください";
            ofd.Filter = "ドキュメントファイル(*.docx)|*.docx";
            ofd.Title = "ソースを保存するドキュメントを選択してください";
            ofd.RestoreDirectory = true;
            ofd.ShowDialog();
            this.sevefilename = ofd.FileName;
            button2.Enabled = false;
            button1.Enabled = true;
        }

        private void Form1_Load(object sender, EventArgs e)
        {
            button1.Enabled = false;
        }

        private void label1_Click(object sender, EventArgs e)
        {

        }
    }
}

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;

namespace MakeSouceList
{
    class FileRead
    {
        public FileRead() { }

        public string filename { get; set; }

        char tmp =(char) 0x0c;
        public string[] readfile()
        {
            if (filename == null) return null;
            StreamReader sr = new StreamReader(this.filename, Encoding.GetEncoding("UTF-8"));

            string result = sr.ReadToEnd();
            string[] test = result.Split('\n');
            for (int i = 0; i < test.Length; i++)
            {
                test[i] += '\n';
                string[] tmp = test[i].Split(this.tmp);
                string tmpstring = null;
                for (int j = 0; j < tmp.Length; j++)
                    tmpstring += tmp[j];
                test[i] = tmpstring;
            }
            
            sr.Close();
            return test;
        }

        public string readfile1()
        {
            if (filename == null) return null;
            StreamReader sr = new StreamReader(this.filename, Encoding.GetEncoding("UTF-8"));
            string result = sr.ReadToEnd();
            string[] test = result.Split('\n');
            return result;
        }
    }
}

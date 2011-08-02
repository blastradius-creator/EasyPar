using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Diagnostics;
using System.IO;

namespace Spawner
{
    class Exec
    {
        public String filename;
        public int[] myCores;
        public String coresSTR;
        public Exec(String filename,  String cores)
        {
            this.filename = filename;
            this.coresSTR = cores;
            String[] coreArr = cores.Split(',');
            myCores = new int[coreArr.Length];

            for (int x = 0; x < coreArr.Length; x++)
            {
                myCores[x] = Convert.ToInt32(coreArr[x].ToString());
            }

            Console.WriteLine("Filename: " + this.filename);
            for (int i = 0; i < myCores.Length; i++)
            {
                Console.WriteLine(myCores[i]);
            }
        }

        public void runMe()
        {
            using (ProcessorAffinity.BeginAffinity(myCores))
            {
                try
                {
                    Console.WriteLine("Running " + filename + " on Cores ["+ this.coresSTR +"]");
                    Console.WriteLine("CurrentWorkingDirectory: " + Directory.GetCurrentDirectory());
                    String arg = @".\" + this.filename;
                    //Console.WriteLine("Arg: " + arg);
                    Process temp_proc = new Process();
                    temp_proc.StartInfo.FileName = "powershell.exe";
                    //temp_proc.StartInfo.FileName = "notepad.exe";
                    temp_proc.StartInfo.WorkingDirectory = Directory.GetCurrentDirectory();
                    temp_proc.StartInfo.UseShellExecute = true;
                    temp_proc.StartInfo.LoadUserProfile = true;
                    temp_proc.StartInfo.RedirectStandardOutput = false;
                    temp_proc.StartInfo.RedirectStandardError = false;
                    temp_proc.StartInfo.RedirectStandardInput = false;
                    temp_proc.StartInfo.Arguments = @"-ExecutionPolicy RemoteSigned -File .\" + this.filename;
                    temp_proc.StartInfo.WindowStyle = ProcessWindowStyle.Normal;
                    temp_proc.StartInfo.CreateNoWindow = true;
                    temp_proc.Start();

                    temp_proc.WaitForExit();
                    int exit_code = temp_proc.ExitCode;
                }
                catch (System.ComponentModel.Win32Exception e)
                {
                    Console.WriteLine(e.Message);
                }
                //Console.WriteLine(output);
            }
        }
    }
}

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Collections;
using System.IO;
using System.Diagnostics;
using System.Threading;
using System.Data.SqlClient;
using System.Security.AccessControl;

namespace Spawner
{
    class Program
    {
        public static ArrayList filenames = new ArrayList();
        public static ArrayList cores = new ArrayList();
        public static List<Exec> ExecList = new List<Exec>();
        public static List<Thread> threads = new List<Thread>();
        private static long execution_time;
        static void Main(string[] args)
        {
            for (int i = 0; i < args.Length; i++)
            {
                if (i % 2 == 0)
                {
                    filenames.Add(args[i]);
                }
                else
                {
                    cores.Add(args[i]);
                }
            }

            for (int i = 0; i < filenames.Count; i++ )
            {
                //Console.WriteLine("Filename: " + filenames[i] + " | Cores: " + cores[i]);
                //Exec Executable = new Exec(filenames[i].ToString(), cores[i].ToString());
                ExecList.Add(new Exec(filenames[i].ToString(), cores[i].ToString()));
            }

            foreach(Exec x in ExecList)
            {
                threads.Add(new Thread(new ThreadStart(x.runMe)));
            }
            Console.WriteLine("Thread Count: " + threads.Count);
            //List<Thread> threads = new List<Thread>;

            doTests();
        }

        private static void doTests()
        {
            Console.WriteLine("Running Executables(" + threads.Count + ")\n");
            //stopwatch begin
            System.Diagnostics.Stopwatch stopwatch = new Stopwatch();
            stopwatch.Start();
            foreach (Thread thread in threads)
            {
                try
                {
                    thread.Start();
                    //Thread.Sleep(3500); //threads start 3.5 seconds after eachother. This gives them some time to copy deployment items so there are no conflicts
                }
                catch (ThreadStateException e)
                {
                    Console.WriteLine("[ERROR] Threading error." + e);
                }
            }

            foreach (Thread thread in threads)
            {
                //give a 90 second timeout
                if (!thread.Join(120000))
                {
                    //Console.WriteLine("One thread timed out and was aborted.");
                    thread.Abort();
                    Console.WriteLine("[ERROR] Threading error. One or more threads timed out.");
                }
            }

            //stopwatch end
            stopwatch.Stop();
            execution_time = stopwatch.ElapsedMilliseconds;
        }

    }
}


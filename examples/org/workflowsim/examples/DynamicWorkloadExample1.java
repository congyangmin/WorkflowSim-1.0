/**
 * Copyright 2012-2013 University Of Southern California
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.workflowsim.examples;

import java.io.File;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import org.cloudbus.cloudsim.CloudletSchedulerDynamicWorkload;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.workflowsim.CondorVM;
import org.workflowsim.WorkflowDatacenter;
import org.workflowsim.Job;
import org.workflowsim.WorkflowEngine;
import org.workflowsim.WorkflowPlanner;
import org.workflowsim.utils.ClusteringParameters;
import org.workflowsim.utils.OverheadParameters;
import org.workflowsim.utils.Parameters;
import org.workflowsim.utils.ReplicaCatalog;

/**
 * This DynamicWorkloadExample1 uses specifically CloudletSchedulerDynamicWorkload as the local scheduler;
 * 该动态调度负载实例在局部Scheduler上特别使用CloudletSchedulerDynamicWorkload
 *
 * @author Weiwei Chen
 * @since WorkflowSim Toolkit 1.0
 * @date Oct 13, 2013
 */
public class DynamicWorkloadExample1 extends WorkflowSimBasicExample1{

    protected static List<CondorVM> createVM(int userId, int vms) {

        //Creates a container to store VMs. This list is passed to the broker later
    	//创建一个容器来存储VMs，该列表随后就会被提交给代理
        LinkedList<CondorVM> list = new LinkedList<CondorVM>();

        //VM Parameters   虚拟机参数
        long size = 10000; //image size (MB) 镜像大小
        int ram = 512; //vm memory (MB)  vm内存
        int mips = 1000;      
        long bw = 1000;
        int pesNumber = 1; //number of cpus
        String vmm = "Xen"; //VMM name

        //create VMs       创建VMs
        CondorVM[] vm = new CondorVM[vms];

        for (int i = 0; i < vms; i++) {
            double ratio = 1.0;
            vm[i] = new CondorVM(i, userId, mips * ratio, pesNumber, ram, bw, size, vmm, new CloudletSchedulerDynamicWorkload(mips * ratio, pesNumber));
            list.add(vm[i]);
        }

        return list;
    }

    ////////////////////////// STATIC METHODS 静态方法///////////////////////
    /**
     * 执行实例的main()方法
     * Creates main() to run this example
     * This example has only one datacenter and one storage
     * 该例子中只有一个数据中心和一个存储器
     */
    public static void main(String[] args) {

        try {
            // First step: Initialize the WorkflowSim package.
        	//第一步：初始化WorkflowSim包

            /**
             * However, the exact number of vms may not necessarily be vmNum If
             * the data center or the host doesn't have sufficient resources the
             * exact vmNum would be smaller than that. Take care.
             * 然而，如果数据中心或者主机并没有重组的资源，那么VMs确切的数量并非就是mNum，实际的mNum要小一些。请注意
             */
            int vmNum = 20;//number of vms;    VMs的数量
            /**
             * Should change this based on real physical path
             * 基于实际的物理路径。
             */
            String daxPath = "D:/OpenGit/WorkflowSim-1.0/config/dax/Montage_100.xml";
           /* if(daxPath == null){
                Log.printLine("Warning: Please replace daxPath with the physical path in your working environment!");
                return;
            }*/
            File daxFile = new File(daxPath);
            if(!daxFile.exists()){
                Log.printLine("Warning: Please replace daxPath with the physical path in your working environment!");
                return;
            }

            /**
             * Since we are using HEFT planning algorithm, the scheduling algorithm should be static 
             * such that the scheduler would not override the result of the planner
             * 因为我们使用HEFT planning算法，该算法应该是静态的，因此Scheduler将不会覆盖Planner的结果。
             */
            Parameters.SchedulingAlgorithm sch_method = Parameters.SchedulingAlgorithm.STATIC;
            Parameters.PlanningAlgorithm pln_method = Parameters.PlanningAlgorithm.HEFT;
            ReplicaCatalog.FileSystem file_system = ReplicaCatalog.FileSystem.LOCAL;

            /**
             * No overheads 
             * 没有开销
             */
            OverheadParameters op = new OverheadParameters(0, null, null, null, null, 0);;
            
            /**
             * No Clustering
             * 没有
             */
            ClusteringParameters.ClusteringMethod method = ClusteringParameters.ClusteringMethod.NONE;
            ClusteringParameters cp = new ClusteringParameters(0, 0, method, null);

            /**
             * Initialize static parameters
             * 初始化静态参数
             */
           
            Parameters.init(vmNum, daxPath, null,
                    null, op, cp, sch_method, pln_method,
                    null, 0);
            ReplicaCatalog.init(file_system);

            // before creating any entities.
            //创建任何实体前
            int num_user = 1;   // number of grid users  用户数量
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false;  // mean trace events   是否记录事件

            // Initialize the CloudSim library       初始化CloudSim包
            CloudSim.init(num_user, calendar, trace_flag);

            WorkflowDatacenter datacenter0 = createDatacenter("Datacenter_0");

            /**
             * Create a WorkflowPlanner with one schedulers.
             * 创建一个具有一个Scheduler的WorkflowPlanner
             */
            WorkflowPlanner wfPlanner = new WorkflowPlanner("planner_0", 1);
            /**
             * Create a WorkflowEngine.
             * 创建一个WorkflowEngine
             */
            WorkflowEngine wfEngine = wfPlanner.getWorkflowEngine();
            /**
             * Create a list of VMs.The userId of a vm is basically the id of the scheduler
             * that controls this vm
             * 创建一个VMs列表，一个VM的用户Id主要是控制该VM的Scheduler的Id。 
             */
            List<CondorVM> vmlist0 = createVM(wfEngine.getSchedulerId(0), Parameters.getVmNum());

            /**
             * Submits this list of vms to this WorkflowEngine.
             * 将VMs列表提交给WorkflowEngine
             */
            wfEngine.submitVmList(vmlist0, 0);

            /**
             * Binds the data centers with the scheduler.
             * 将Scheduler和数据中心绑定
             */
            wfEngine.bindSchedulerDatacenter(datacenter0.getId(), 0);

            CloudSim.startSimulation();


            List<Job> outputList0 = wfEngine.getJobsReceivedList();

            CloudSim.stopSimulation();

            printJobList(outputList0);
            

        } catch (Exception e) {
            Log.printLine("The simulation has been terminated due to an unexpected error");
        }
    }

}

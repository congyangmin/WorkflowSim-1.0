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
package org.workflowsim.scheduling;

import java.util.ArrayList;
import java.util.List;
import org.cloudbus.cloudsim.Cloudlet;
import org.workflowsim.CondorVM;
import org.workflowsim.WorkflowSimTags;

/**
 * MinMin algorithm.
 *
 * @author Weiwei Chen
 * @since WorkflowSim Toolkit 1.0
 * @date Apr 9, 2013
 */
public class MinMinSchedulingAlgorithm extends BaseSchedulingAlgorithm {

    public MinMinSchedulingAlgorithm() {
        super();
    }
    private List hasChecked = new ArrayList<Boolean>();

    @Override
    public void run() {

        int size = getCloudletList().size();          //获取job任务列表大小
        hasChecked.clear();
        for (int t = 0; t < size; t++) {
            hasChecked.add(false);           //初始化为false
        }
        for (int i = 0; i < size; i++) {
            int minIndex = 0;
            Cloudlet minCloudlet = null;
            for (int j = 0; j < size; j++) {
                Cloudlet cloudlet = (Cloudlet) getCloudletList().get(j);       //从job list中获得cloudlet对象
                boolean chk = (Boolean) (hasChecked.get(j));        
                if (!chk) {
                    minCloudlet = cloudlet;               
                    minIndex = j;
                    break;
                }
            }
            if (minCloudlet == null) {
                break;                    
            }


            for (int j = 0; j < size; j++) {
                Cloudlet cloudlet = (Cloudlet) getCloudletList().get(j);
                boolean chk = (Boolean) (hasChecked.get(j));

                if (chk) {
                    continue;
                }

                long length = cloudlet.getCloudletLength();

                if (length < minCloudlet.getCloudletLength()) {     //此处与MaxMin不同，此处选择最短的任务，而MaxMin中选择最大的任务
                    minCloudlet = cloudlet;
                    minIndex = j;
                }
            }
            hasChecked.set(minIndex, true);

            int vmSize = getVmList().size();             //获取VM大小
            CondorVM firstIdleVm = null;//(CondorVM)getVmList().get(0);
            for (int j = 0; j < vmSize; j++) {
                CondorVM vm = (CondorVM) getVmList().get(j);            
                if (vm.getState() == WorkflowSimTags.VM_STATUS_IDLE) {      
                    firstIdleVm = vm;
                    break;
                }
            }
            if (firstIdleVm == null) {
                break;
            }
            //调度的关键步骤
            for (int j = 0; j < vmSize; j++) {
                CondorVM vm = (CondorVM) getVmList().get(j);          //获取当前虚拟机对象
                if ((vm.getState() == WorkflowSimTags.VM_STATUS_IDLE)       //VM是否空闲？
                        && vm.getCurrentRequestedTotalMips() > firstIdleVm.getCurrentRequestedTotalMips()) {
                    firstIdleVm = vm;

                }
            }
            firstIdleVm.setState(WorkflowSimTags.VM_STATUS_BUSY);         //将VM设置为忙碌
            minCloudlet.setVmId(firstIdleVm.getId());         //为每一个云任务（job）设置一个VM Id号
            getScheduledList().add(minCloudlet);        //将cloudlet添加到调度列表中

        }
    }
}

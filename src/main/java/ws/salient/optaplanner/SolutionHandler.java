/*
 * Copyright 2016 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ws.salient.optaplanner;

import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.inject.Inject;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SolutionHandler implements WorkItemHandler {

    private static final Logger log = LoggerFactory.getLogger(SolutionHandler.class);

    private final KieContainer container;
    private final ConcurrentMap<String, SolverFactory> solverFactories;
    
    @Inject
    public SolutionHandler(KieContainer container) {
        this.container = container;
        solverFactories = new ConcurrentHashMap();
    }

    @Override
    public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
        String solverConfig = (String) workItem.getParameter("solver");
        Object unsolved = workItem.getParameter("problem");
        SolverFactory solverFactory = solverFactories.computeIfAbsent(solverConfig, (config) -> {
            return SolverFactory.createFromKieContainerXmlResource(container, config);
        });
        Solver solver = solverFactory.buildSolver();
        solver.solve(unsolved);
        manager.completeWorkItem(workItem.getId(), Collections.singletonMap("solution", solver.getBestSolution()));
    }

    @Override
    public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {

    }

}

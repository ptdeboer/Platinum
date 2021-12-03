package nl.esciencecenter.vbrowser.vrs.io.copy;

import lombok.extern.slf4j.Slf4j;
import nl.esciencecenter.ptk.data.ExtendedList;
import nl.esciencecenter.ptk.presentation.Presentation;
import nl.esciencecenter.ptk.task.ITaskMonitor;
import nl.esciencecenter.ptk.task.MonitorStats;
import nl.esciencecenter.ptk.task.TaskMonitorAdaptor;
import nl.esciencecenter.vbrowser.vrs.VFSPath;
import nl.esciencecenter.vbrowser.vrs.VPath;
import nl.esciencecenter.vbrowser.vrs.exceptions.ResourceNotFoundException;
import nl.esciencecenter.vbrowser.vrs.exceptions.ResourceTypeMismatchException;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class HeapCopy {

    public class HeapCopyElement {
        protected VPath sourcePath;
        protected String sourceType;
        protected VFSPath destDirPath;
        protected long size;
        protected boolean isDeleted = false;
        public boolean isDone = false;
        public VFSPath resolvedDestPath;

        public HeapCopyElement(VPath path, String type) {
            this.sourcePath = path;
            this.sourceType = type;
        }
    }

    protected List<? extends VPath> orgSources;

    protected ExtendedList<HeapCopyElement> pathHeap;

    protected VFSPath targetDirPath;

    protected boolean isMove;

    protected ITaskMonitor monitor;

    private List<VFSPath> resultPaths;

    private List<VPath> deletedPaths;

    private final VRSCopyManager copyManager;

    private final String actionStr;

    private boolean started;

    private boolean stopped;

    long totalBytesTodo = 0;

    long totalBytesCopied = 0;

    Object mutex = new Object();

    public HeapCopy(VRSCopyManager copyManager, List<? extends VPath> sources, VFSPath targetDirPath, boolean isMove,
                    ITaskMonitor optMonitor) {
        //
        this.actionStr = ((isMove == true) ? "HeapMoving" : "HeapCopying");
        this.copyManager = copyManager;
        this.orgSources = sources;
        this.targetDirPath = targetDirPath;
        this.monitor = optMonitor;
        this.isMove = isMove;
        this.started = false;
        this.stopped = false;
    }

    public void copy() throws VrsException {
        //
        synchronized (mutex) {
            if (started) {
                throw new RuntimeException("copy() can NOT be called again!");
            }
            this.started = true;
        }

        try {
            pre();
            doScan();
            doCopy();
            post();
        } catch (Throwable t) {
            if (monitor != null) {
                this.monitor.setException(t);
            }
            throw new VrsException("Copy Failed!\n" + t.getMessage(), t);
        } finally {
            this.stopped = true;
        }
    }

    protected void pre() {
        totalBytesTodo = 0;
        totalBytesCopied = 0;
        resultPaths = new ArrayList<VFSPath>();
        deletedPaths = new ArrayList<VPath>();
        pathHeap = new ExtendedList<HeapCopyElement>();

        if (monitor == null) {
            monitor = new TaskMonitorAdaptor(actionStr, (orgSources != null) ? orgSources.size() : 0);
        }
    }

    public ITaskMonitor getMonitor() {
        return monitor;
    }

    protected void post() {
    }

    private boolean mustStop() {
        if ((monitor != null) && (monitor.isCancelled())) {
            return true;
        }
        // general contract of unhandled interrupt is to stop.
        if (Thread.currentThread().isInterrupted()) {
            // forward, if not already done, to monitor.
            if (monitor != null) {
                monitor.setIsCancelled();
            }
            return true;
        }
        return false;
    }

    protected void doScan() throws VrsException, InterruptedException {
        ArrayList<VPath> nodes = new ArrayList<VPath>();

        if ((orgSources == null) || (orgSources.size() <= 0)) {
            return;
        }

        // Pass one: scan toplevel current VRLs:
        for (int i = 0; i < orgSources.size(); i++) {
            if (mustStop()) {
                throw new InterruptedException("Got cancelled");
            }

            VPath vpath = orgSources.get(i);
            nodes.add(vpath);
            log.debug(" - toplevel path:{}", vpath);
        }
        // Pass two: depth first recursive scan.
        heapScan(this.targetDirPath, nodes);

        logPrintf(" - total bytes=%s (%s bytes)\n", Presentation.createSizeString(totalBytesTodo, true, 1, 6),
                totalBytesTodo);
    }

    private void heapScan(VFSPath targetDir, List<? extends VPath> nodes) throws VrsException {
        // Recursive heap scan, composite first, leafs later.
        for (VPath node : nodes) {
            if (node.isComposite()) {
                VFSPath subTargetDir = targetDir.resolve(node.getVRL().getBasename());

                logPrintf(" - scanning directory:%s", node.getVRL());
                heapAddPath(targetDir, node, node.getResourceType());
                // recursive add, depth first!

                if ((isMove) && isSameFileSystem(targetDirPath, node)) {
                    // stop here if it is a fileSystem move.
                } else {
                    heapScan(subTargetDir, node.list());
                }
            }
        }

        // add leaf nodes:
        for (VPath node : nodes) {
            if (!node.isComposite()) {
                heapAddPath(targetDir, node, node.getResourceType());
            }
        }
    }

    private boolean isSameFileSystem(VFSPath vfsPath, VPath node) throws VrsException {
        return (vfsPath.getFileSystem().equals(node.getResourceSystem()));
    }

    private void heapAddPath(VFSPath targetDirPath, VPath vpath, String type) throws VrsException {
        log.debug(" - adding path:{}", vpath);
        HeapCopyElement el = new HeapCopyElement(vpath, type);
        if (vpath instanceof VFSPath) {
            el.size = ((VFSPath) vpath).fileLength();
            totalBytesTodo += el.size;

        }
        el.destDirPath = targetDirPath;
        pathHeap.add(el);
    }

    public List<VFSPath> getResultPaths() {
        return this.resultPaths;
    }

    public List<VPath> getDeletedPaths() {
        return this.deletedPaths;
    }

    protected void doCopy() throws VrsException, InterruptedException {
        monitorStartCopy(totalBytesTodo);

        for (int i = 0; i < pathHeap.size(); i++) {
            if (mustStop()) {
                throw new InterruptedException("Got cancelled");
            }

            HeapCopyElement heapEl = pathHeap.get(i);
            VPath sourcePath = heapEl.sourcePath;
            boolean copyAll = true;

            if (sourcePath instanceof VFSPath) {
                boolean status = false;

                VFSPath resolvedTargetPath = heapEl.destDirPath.resolve(sourcePath.getVRL().getBasename());
                heapEl.resolvedDestPath = resolvedTargetPath;

                log.debug("Resolved targetFile: '{}' + '{}' => '{}'", targetDirPath.getVRL(), sourcePath
                        .getVRL().getBasename(), resolvedTargetPath.getVRL());

                VFSPath vfsPath = (VFSPath) sourcePath;

                if (vfsPath.exists() == false) {
                    // can happen if directory changed since last scan.
                    throw new ResourceNotFoundException("Source path doesn't exists!. Has it been moved ?:" + vfsPath,
                            null);
                } else if (vfsPath.isDir()) {
                    logPrintf(" - %s directory:%s => %s\n", actionStr, sourcePath.getVRL(),
                            resolvedTargetPath.getVRL());

                    if ((isMove) && (isSameFileSystem(vfsPath, resolvedTargetPath))) {
                        // sourcePath must be VFSPath:
                        status = copyManager.fileSystemRename((VFSPath) sourcePath, resolvedTargetPath, monitor);
                    } else {
                        status = resolvedTargetPath.mkdir(true);
                    }
                } else if (copyAll || vfsPath.isFile()) {
                    logPrintf(" - %s file:%s => %s\n", actionStr, sourcePath.getVRL(),
                            resolvedTargetPath.getVRL());
                    status = copyManager.doCopyMoveResourceToFile(vfsPath, resolvedTargetPath, isMove, monitor);
                } else {
                    logPrintf(" - Error: Unknown VFS resource:%s\n", vfsPath);
                    throw new ResourceTypeMismatchException("Can not copy VFSPath:" + vfsPath, null);
                }

                if (status) {
                    resultPaths.add(resolvedTargetPath);
                    if (isMove) {
                        deletedPaths.add(sourcePath);
                        heapEl.isDeleted = true;
                    }
                    heapEl.isDone = true;
                } else {
                    throw new VrsException("Invalid state, copy or move went wrong for:" + sourcePath);
                }
            } else {
                logPrintf(" - Error: non VFS Path:%s\n", sourcePath);
                throw new ResourceTypeMismatchException("Can not copy:" + sourcePath, null);
            }

            // update total done:
            if (heapEl.size > 0) {
                totalBytesCopied += heapEl.size;
                monitorUpdateBytesCopied(totalBytesCopied);
            }
        }
    }

    private void monitorUpdateBytesCopied(long numBytes) {
        if (monitor != null) {
            monitor.updateSubTaskDone("" + MonitorStats.MonitorStatsType.TOTAL_BYTES_TRANSFERRED, numBytes);
            monitor.updateTaskDone((numBytes));
        }
    }

    private void monitorStartCopy(long numBytes) {
        if (monitor != null) {
            monitor.startTask("HeapCopy", numBytes);
            monitor.startSubTask("" + MonitorStats.MonitorStatsType.TOTAL_BYTES_TRANSFERRED, numBytes);
        }
    }

    private void logPrintf(String format, Object... args) {
        if (monitor != null) {
            monitor.logPrintf(format, args);
        }
    }
}

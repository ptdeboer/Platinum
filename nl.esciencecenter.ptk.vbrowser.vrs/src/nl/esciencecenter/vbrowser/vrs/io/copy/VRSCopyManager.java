/*
 * Copyright 2012-2014 Netherlands eScience Center.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at the following location:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * For the full license, see: LICENSE.txt (located in the root folder of this distribution).
 * ---
 */
// source:

package nl.esciencecenter.vbrowser.vrs.io.copy;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.LinkOption;
import java.util.ArrayList;
import java.util.List;

import nl.esciencecenter.ptk.data.ExtendedList;
import nl.esciencecenter.ptk.data.Holder;
import nl.esciencecenter.ptk.data.VARHolder;
import nl.esciencecenter.ptk.data.VARListHolder;
import nl.esciencecenter.ptk.io.IOUtil;
import nl.esciencecenter.ptk.task.ITaskMonitor;
import nl.esciencecenter.ptk.util.logging.ClassLogger;
import nl.esciencecenter.vbrowser.vrs.VFSPath;
import nl.esciencecenter.vbrowser.vrs.VFileSystem;
import nl.esciencecenter.vbrowser.vrs.VPath;
import nl.esciencecenter.vbrowser.vrs.VRSClient;
import nl.esciencecenter.vbrowser.vrs.VResourceSystem;
import nl.esciencecenter.vbrowser.vrs.data.AttributeSet;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.infors.VInfoResource;
import nl.esciencecenter.vbrowser.vrs.infors.VInfoResourcePath;
import nl.esciencecenter.vbrowser.vrs.io.VPathDeletable;
import nl.esciencecenter.vbrowser.vrs.io.VStreamReadable;
import nl.esciencecenter.vbrowser.vrs.io.VStreamWritable;
import nl.esciencecenter.vbrowser.vrs.task.VRSTaskWatcher;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

/**
 * Default Copy and move manager to perform both VFS and non VFS Copy/Move
 * actions. Not all resources can be read from or written to.
 */
public class VRSCopyManager
{
    private static final ClassLogger logger = ClassLogger.getLogger(VRSCopyManager.class);

    // ========
    // Instance
    // ========

    protected VRSClient vrsClient;

    protected VRSTaskWatcher taskWatcher;

    public VRSCopyManager(VRSClient vrsClient)
    {
        this.vrsClient = vrsClient;
        // Use static instance for now:
        this.taskWatcher = VRSTaskWatcher.getTaskWatcher();
    }

    // ===
    // Implementation
    // ===

    public boolean doLinkDrop(List<VRL> vrls, VRL destVrl, VARHolder<VPath> destPathH, VARListHolder<VPath> resultNodesH,
            ITaskMonitor monitor) throws VrsException
    {
        String taskName = "LinkDrop on:" + destVrl;
        logger.debugPrintf("doLinkDrop():%s, vrls=%s\n", destVrl, new ExtendedList<VRL>(vrls));
        monitor.startTask(taskName, vrls.size());
        monitor.logPrintf("LinkDrop on:%s, vrls=\n", destVrl);
        
        try
        {
            VPath destPath = this.vrsClient.openPath(destVrl);
            if (destPathH != null)
            {
                destPathH.set(destPath);
            }

            int index = 0;

            ArrayList<VPath> nodes = new ArrayList<VPath>();

            for (VRL vrl : vrls)
            {
                VPath sourcePath=vrsClient.openPath(vrl); 
                monitor.logPrintf(" - linkDrop: %s\n", vrl);
                VInfoResourcePath newNode;
                
                if (destPath instanceof VInfoResourcePath)
                {
                    VInfoResourcePath infoDest=((VInfoResourcePath) destPath); 
                    
                    if (sourcePath instanceof VInfoResourcePath)
                    {
                        logger.infoPrintf("Copying Actual ResourceNode attributes to create link to:%s\n",sourcePath);
                        // copy/link the original ResourceNode and do not create a link-to-a-link(!) 
                        
                        VInfoResourcePath infoSource=(VInfoResourcePath)sourcePath; 
                        AttributeSet infoAttrs=infoSource.getInfoAttributes(); 
                        newNode=((VInfoResourcePath) destPath).createSubNode(sourcePath.getResourceType(), infoAttrs); 
                    }
                    else
                    {
                        newNode = infoDest.createResourceLink(vrl, "Link to:" + vrl.getBasename());
                    }
                    monitor.logPrintf(" -created new ResourceNode:%s\n", newNode);
                    nodes.add(newNode);
                }
                else if (destPath instanceof VFSPath)
                {
                    logger.errorPrintf(" - FIXME: linkDrop on:%s<<%s\n", destVrl, vrl);
                    monitor.logPrintf(" - FIXME: linkDrop on:%s<<%s\n", destVrl, vrl);

                    try
                    {
                        Thread.sleep(250);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }

                    return false;
                }
                else
                {
                    throw new VrsException("LinkDrop not supported on resource:" + destVrl);
                }

                monitor.updateTaskDone(++index);
            }
            resultNodesH.set(nodes);
            monitor.endTask(taskName);
            return true;
        }
        catch (Throwable t)
        {
            monitor.setException(t);
            monitor.logPrintf("Exception:%s\n", t);
            throw new VrsException(t.getMessage(), t);
        }

        // return false;
    }

    public boolean doCopyMove(List<VRL> vrls, VRL destVrl, boolean isMove, VARHolder<VPath> destPathH, VARListHolder<VPath> resultPathsH,
            VARListHolder<VPath> deletedNodesH, ITaskMonitor monitor) throws VrsException
    {
        logger.errorPrintf("***FIXME:doCopyMove():%s, vrls=%s\n", destVrl, new ExtendedList<VRL>(vrls));

        // Pre Checks:
        if ((vrls == null) || (vrls.size() < 1))
        {
            throw new VrsException("No resource to copy/move");
        }

        String actionStr=((isMove==true)?"Moving":"Copying")+" to:"+destVrl; 
        monitor.startTask(actionStr, vrls.size());
        
        monitor.logPrintf("%s.\n", actionStr); 
        int index = 0;

        VPath destPath = vrsClient.openPath(destVrl);
        VPath firstPath = vrsClient.openPath(vrls.get(0));

        if (destPath instanceof VFSPath)
        {
            VFSPath vfsDestPath = (VFSPath) destPath;
            destPathH.set(destPath);
            boolean status;

            if (vfsDestPath.isDir())
            {
                status = doCopyMoveToDir(vrls, vfsDestPath, isMove, resultPathsH, deletedNodesH, monitor);
            }
            else if (vfsDestPath.isFile())
            {
                if (vrls.size() > 1)
                {
                    // could be arguments to start a script/binary here
                    throw new VrsException("Cannot drop multiple resources onto a single file!");
                }

                status = doCopyMoveResourceToFile(firstPath, vfsDestPath, isMove, monitor);
                if (isMove)
                {
                    ArrayList<VPath> deletedPaths = new ArrayList<VPath>();
                    deletedPaths.add(firstPath);
                    deletedNodesH.set(deletedPaths);
                }
            }
            else
            {
                throw new VrsException("Don't know how to copy to VFS Path:" + destPath);
            }

            monitor.endTask(actionStr);
            return status;
        }

        monitor.endTask(actionStr);

        throw new VrsException("Don't know how to copy/move to:" + destPath);
    }

    private boolean doCopyMoveResourceToFile(VPath sourcePath, VFSPath targetPath, boolean isMove, ITaskMonitor monitor)
            throws VrsException
    {
        logger.errorPrintf("doCopyMoveResourceToFile: source:%s\n", sourcePath);
        logger.errorPrintf("doCopyMoveResourceToFile: dest  :%s\n", targetPath);

        VResourceSystem sourceRs = sourcePath.getResourceSystem();
        VFileSystem targetVFS = targetPath.getFileSystem();

        if ((isMove) && (targetVFS.equals(sourceRs)))
        {
            // sourcePath must be VFSPath:
            return fileSystemRename((VFSPath)sourcePath,targetPath,monitor); 
        }

        VPathDeletable deletable = null;

        // ---------------------------
        // Check move.
        // A cross resource system Move-Drop is a Copy+Delete (like windows).
        // ---------------------------

        if (isMove)
        {
            if (sourcePath instanceof VPathDeletable)
            {
                deletable = (VPathDeletable) sourcePath;
            }
            else
            {
                throw new VrsException("Can not move resource(s) if original source path can't be deleted:" + sourcePath);
            }
        }

        // ---------------------------
        // Default is stream copy to target file.
        // ---------------------------
        streamCopyFile(sourcePath, targetPath, monitor);

        if (isMove)
        {
            deletable.delete(false);
        }

        return true;
    }

    private boolean fileSystemRename(VFSPath sourcePath, VFSPath targetPath,ITaskMonitor monitor) throws VrsException
    {
        // rename/move on same filesystem
        String renameTask = "Renaming:" + sourcePath.getVRL().getPath() + " to:" + targetPath.getVRL().getPath();

        monitor.startSubTask(renameTask, 1);
        monitor.logPrintf("%s\n", renameTask);
        VPath newPath = sourcePath.renameTo(targetPath.getVRL().getPath());
        monitor.updateSubTaskDone(renameTask, 1);
        monitor.endSubTask(renameTask);
        return true;

    }

    private boolean doCopyMoveToDir(List<VRL> vrls, VFSPath targetDirPath, boolean isMove, VARListHolder<VPath> resultPathsH,
            VARListHolder<VPath> deletedNodesH, ITaskMonitor monitor) throws VrsException
    {

        String actionStr=((isMove==true)?"Moving":"Copying"); 
        
        monitor.logPrintf("%s to directory:%s.\n", actionStr,targetDirPath.getVRL());

        List<VPath> resultPaths = new ArrayList<VPath>();
        resultPathsH.set(resultPaths);

        List<VPath> deletedPaths = new ArrayList<VPath>();
        deletedNodesH.set(deletedPaths);

        for (VRL vrl : vrls)
        {
            VPath sourcePath = vrsClient.openPath(vrl);

            if (sourcePath instanceof VFSPath)
            {
                VFSPath actualTargetFile = targetDirPath.resolvePath(sourcePath.getVRL().getBasename());
                
                logger.debugPrintf("Resolved targetFile: '%s' + '%s' => '%s'\n", targetDirPath.getVRL(), sourcePath.getVRL().getBasename(),
                        actualTargetFile.getVRL());

                monitor.logPrintf(" - %s resource:%s => %s\n",actionStr, vrl,actualTargetFile.getVRL());
                
                VFSPath vfsPath = (VFSPath) sourcePath;

                if (vfsPath.isFile())
                {
                    this.doCopyMoveResourceToFile(vfsPath, actualTargetFile, isMove, monitor);
                    resultPaths.add(actualTargetFile);
                    if (isMove)
                    {
                        deletedPaths.add(sourcePath);
                    }
                }
                else
                {
                    monitor.logPrintf(" - Skipping Directory:%s\n", vfsPath);
                }
            }
            else
            {
                monitor.logPrintf(" - Skipping non VFS Path:%s\n", sourcePath);
            }
        }

        // DirHeapCopy dirHeapCopy=new DirHeapCopy(vrls,dirPath);
        return (resultPaths.size() > 0);
    }

    public void streamCopyFile(VPath sourcePath, VFSPath targetFile, ITaskMonitor monitor) throws VrsException
    {

        if ((sourcePath instanceof VStreamReadable) == false)
        {
            throw new VrsException("Can not read from input source:" + sourcePath);
        }

        if ((targetFile instanceof VStreamWritable) == false)
        {
            throw new VrsException("Can not write to target file (not stream writable):" + targetFile);
        }

        // actual copy:

        try
        {
            boolean targetExists = targetFile.exists(LinkOption.NOFOLLOW_LINKS);

            long len = -1;
            if (sourcePath instanceof VFSPath)
            {
                VFSPath vfsPath = (VFSPath) sourcePath;
                len = vfsPath.getLength();
            }

            InputStream inps = ((VStreamReadable) sourcePath).createInputStream();
            OutputStream outps = ((VStreamWritable) targetFile).createOutputStream(false);

            IOUtil.circularStreamCopy(inps, outps, len, 1024 * 1024, false, monitor);

            IOUtil.autoClose(inps);
            IOUtil.autoClose(outps);

        }
        catch (Exception e)
        {
            throw new VrsException("Copy Failed:" + e.getMessage(), e);
        }

    }
}

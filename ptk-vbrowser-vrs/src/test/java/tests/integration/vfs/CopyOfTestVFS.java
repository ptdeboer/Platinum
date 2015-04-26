///*
// * Copyright 2006-2010 Virtual Laboratory for e-Science (www.vl-e.nl)
// * Copyright 2012-2013 Netherlands eScience Center.
// *
// * Licensed under the Apache License, Version 2.0 (the "License").
// * You may not use this file except in compliance with the License. 
// * You may obtain a copy of the License at the following location:
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// * 
// * For the full license, see: LICENSE.txt (located in the root folder of this distribution).
// * ---
// */
//// source:
//
//package tests.integration.vfs;
//
//import static nl.esciencecenter.vbrowser.vrs.data.AttributeNames.ATTR_FILE_SIZE;
//import static nl.esciencecenter.vbrowser.vrs.data.AttributeNames.ATTR_HOSTNAME;
//import static nl.esciencecenter.vbrowser.vrs.data.AttributeNames.ATTR_MIMETYPE;
//import static nl.esciencecenter.vbrowser.vrs.data.AttributeNames.ATTR_PATH;
//import static nl.esciencecenter.vbrowser.vrs.data.AttributeNames.ATTR_PORT;
//import static nl.esciencecenter.vbrowser.vrs.data.AttributeNames.ATTR_RESOURCE_TYPE;
//import static nl.esciencecenter.vbrowser.vrs.data.AttributeNames.ATTR_SCHEME;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.net.URISyntaxException;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Random;
//
//import nl.esciencecenter.ptk.GlobalProperties;
//import nl.esciencecenter.ptk.data.IntegerHolder;
//import nl.esciencecenter.ptk.data.StringList;
//import nl.esciencecenter.ptk.net.URIFactory;
//import nl.esciencecenter.ptk.task.ITaskMonitor;
//import nl.esciencecenter.ptk.util.StringUtil;
//import nl.esciencecenter.vbrowser.vrs.VFSPath;
//import nl.esciencecenter.vbrowser.vrs.VFileSystem;
//import nl.esciencecenter.vbrowser.vrs.VRSClient;
//import nl.esciencecenter.vbrowser.vrs.VRSContext;
//import nl.esciencecenter.vbrowser.vrs.data.Attribute;
//import nl.esciencecenter.vbrowser.vrs.exceptions.ResourceCreationException;
//import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
//import nl.esciencecenter.vbrowser.vrs.io.VReplicatable;
//import nl.esciencecenter.vbrowser.vrs.registry.ResourceSystemInfo;
//import nl.esciencecenter.vbrowser.vrs.vrl.VRL;
//
//import org.junit.After;
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//
//import tests.integration.vfs.TestSettings.TestLocation;
//
//
//
///**
// * This is an abstract test class which must be subclassed by a VFS
// * implementation.
// * 
// * 
// * @author P.T. de Boer
// */
//
//public class CopyOfTestVFS extends TestVFS
//{
//   
//    // =======================================================================
//    // VFSPath.list() filter tests
//    // =======================================================================
//
//    @Test public void testListDirFiltered() throws Exception
//    {
//        VFSPath ldir = createRemoteDir("dirListTest",false);
//
//        // list EMPTY dir:
//        List<? extends VFSPath> nodes = ldir.list();
//
//        if ((nodes != null) && (nodes.size() > 0))
//        {
//            // previous junit test was aborted.
//            try
//            {
//                ldir.delete(true);
//            }
//            catch (Exception e)
//            {
//                ;
//            }
//            Assert.fail("Pre condition failed: New created directory must be empty. Please Run junit test again");
//        }
//
//        try
//        {
//            ldir.resolvePath("file0").createFile(false); 
//            ldir.resolvePath("file1.txt").createFile(false); 
//            ldir.resolvePath("file2.aap").createFile(false); 
//            ldir.resolvePath("file3.aap.txt").createFile(false); 
//
//            // check plain list():
//            List<? extends VFSPath> result = ldir.list();
//            Assert.assertNotNull("List result may not be null", result);
//            Assert.assertEquals("Number of returned files is not correct.", 4, result.size());
//
//            result = ldir.list("*", false);
//            Assert.assertNotNull("List result may not be null", result);
//            Assert.assertEquals("Number of returned files is not correct.", 4, result.size());
//
//            message("nr of filtered files '*' =" + result.size());
//
//            result = ldir.list("*.txt", false);
//            Assert.assertNotNull("List result may not be null", result);
//            Assert.assertEquals("Number of returned files is not correct.", 2, result.size());
//
//            message("nr of filtered files '*.txt' =" + result.size());
//
//            // test RE version of *.txt
//            result = ldir.list(".*\\.txt", true);
//            Assert.assertNotNull("List result may not be null", result);
//            Assert.assertEquals("Number of returned files is not correct.", 2, result.size());
//
//            message("nr of filtered files '.*\\.txt' =" + result.size());
//
//        }
//        finally
//        {
//            ldir.delete(true);
//        }
//
//    }
//
//    @Test public void testListDirIterator() throws Exception
//    {
//        VFSPath ldir = createRemoteDir("dirListTest2",false);
//        String fileName0 = "file0";
//        String fileName1 = "file1";
//        String fileName2 = "file2";
//
//        try
//        {
//            ldir.resolvePath(fileName0).createFile(false);
//            ldir.resolvePath(fileName1).createFile(false);
//            ldir.resolvePath(fileName2).createFile(false);
//            ldir.resolvePath("file3").createFile(false);
//            
//            IntegerHolder totalNumNodes = new IntegerHolder();
//
//            // ===
//            // Test range combinations.
//            // ===
//
//            // complete range:
//
//            VFSPath[] totalResult = ldir.list(0, -1, totalNumNodes);
//            Assert.assertNotNull("List result may not be null.", totalResult);
//            Assert.assertEquals("Number of returned files is not correct.", 4, totalResult.length);
//            Assert.assertTrue("IntegerHolder may not contain NULL value.", totalNumNodes.isSet());
//            
//            if (totalNumNodes.intValue()<0)
//                warnPrintf("testListDirIterator(): totalNumNodes not supported!\n"); 
//            else
//                Assert.assertEquals("Total Number of nodes is not correct.", 4, totalNumNodes.intValue());
//            // get first file:
//
//            VFSPath[] result = ldir.list(0, 1, totalNumNodes);
//
//            Assert.assertNotNull("List result may not be null.", result);
//            Assert.assertEquals("Number of returned files is not correct.", 1, result.length);
//            Assert.assertTrue("IntegerHolder may not contain NULL value.", totalNumNodes.isSet());
//            if (totalNumNodes.intValue()<0)
//                warnPrintf("testListDirIterator(): totalNumNodes not supported!\n"); 
//            else
//                Assert.assertEquals("Total Number of nodes is not correct.", 4, totalNumNodes.intValue());
//            // Check with filename with entry in complete list
//            Assert.assertEquals("Returned result is not correct.", result[0].getName(), totalResult[0].getName());
//            if (totalNumNodes.intValue()<0)
//                warnPrintf("testListDirIterator(): totalNumNodes not supported!\n"); 
//            else
//                Assert.assertEquals("Total Number of nodes is not correct.", 4, totalNumNodes.intValue());
//            message("test ListIterator Single result is =" + result[0]);
//
//            result = ldir.list(1, 2, null);
//            Assert.assertNotNull("List result may not be null.", result);
//            Assert.assertEquals("Number of returned files is not correct.", 2, result.length);
//            Assert.assertTrue("IntegerHolder may not contain NULL value.", totalNumNodes.isSet());
//            // check entry against totalResult
//            Assert.assertEquals("Returned result is not correct.", result[0].getName(), totalResult[1].getName());
//            Assert.assertEquals("Returned result is not correct.", result[1].getName(), totalResult[2].getName());
//
//            message("test ListIterator Single result is =" + result[0]);
//
//            result = ldir.list(2, 1, totalNumNodes);
//
//            Assert.assertNotNull("List result may not be null.", result);
//            Assert.assertEquals("Number of returned files is not correct.", 1, result.length);
//            Assert.assertTrue("IntegerHolder may not contain NULL value.", totalNumNodes.isSet());
//
//            if (totalNumNodes.intValue()<0)
//                warnPrintf("testListDirIterator(): totalNumNodes not supported!\n"); 
//            else
//                Assert.assertEquals("Total Number of nodes is not correct.", 4, totalNumNodes.intValue());
//
//            // Check with filename with entry #2 in complete list
//            Assert.assertEquals("Returned result is not correct.", result[0].getName(), totalResult[2].getName());
//
//            message("test ListIterator Single result is =" + result[0]);
//        }
//        finally
//        {
//            ldir.delete(true);
//        }
//    }
//
//    // =======================================================================
//    // Other
//    // =======================================================================
//
//    @Test public void testACLs() throws Exception
//    {
//        VFSPath dir = this.getRemoteTestDir();
//
//        // VFSPath read returns readonly ACL, so is never NULL, unless
//        // an implementation screwed up.
//
//        Attribute[][] acl = dir.getACL();
//        Assert.assertNotNull("ACLs for directoires not supported: ACL is NULL", acl);
//
//        VFSPath file = dir.createFile("aclTestFile", true);
//
//        acl = file.getACL();
//        Assert.assertNotNull("ACLs not for files supported: ACL is NULL", acl);
//
//        // entities are default NULL, so stop testing when no entities are
//        // present
//        Attribute[] ents = dir.getACLEntities();
//
//        // no entities: no support for ACLs: allowed
//        if (ents == null)
//        {
//            file.delete();
//            return;
//        }
//
//        Assert.assertFalse("non NULL ACL entity list may not be empty.", ents.length == 0);
//
//        // get 1st entity
//        Attribute entity = ents[0];
//
//        Attribute[] record = file.createACLRecord(entity, true);
//        Assert.assertNotNull("new entity returned NULL", record);
//
//        file.delete();
//    }
//
//    // =======================================================================
//    // Logical File Alias and Links:
//    // =======================================================================
//
//    @Test public void testLogicalAlias() throws Exception
//    {
//        VFSPath orgFile = createRemoteFile("testLinkFileOriginal-1", true);
//        String link1name = "testLinkto-1";
//        String link2name = "testLinkto-2";
//        VRL link1 = getRemoteTestDir().resolvePath(link1name);
//        VRL link2 = getRemoteTestDir().resolvePath(link2name);
//
//        if (existsFile(getRemoteTestDir(),link1name))
//        {
//            message("*** Warning: test link already exists. previous junit test failed. Removing:" + link1);
//            getRemoteTestDir().deleteFile(link1name);
//        }
//
//        if (existsFile(getRemoteTestDir(),link2name))
//        {
//            message("*** Warning: test link already exists. previous junit test failed. Removing:" + link2);
//            getRemoteTestDir().deleteFile(link2name);
//        }
//
//        try
//        {
//            if (orgFile instanceof VLogicalFileAlias)
//            {
//                VLogicalFileAlias lfn = (VLogicalFileAlias) orgFile;
//
//                link1 = lfn.addAlias(link1);
//                link2 = lfn.addAlias(link2);
//
//                VRL vrls[] = lfn.getLinksTo();
//                boolean hasLink1 = false;
//                boolean hasLink2 = false;
//
//                for (VRL vrl : vrls)
//                {
//                    if (vrl.equals(link1))
//                    {
//                        message("Original file has new link(1):" + link1);
//                        hasLink1 = true;
//                    }
//                    if (vrl.equals(link2))
//                    {
//                        message("Original file has new link(2):" + link2);
//                        hasLink2 = true;
//                    }
//                }
//
//                Assert.assertTrue("New added link(1) not returned by getLinksTo():" + link1, hasLink1);
//                Assert.assertTrue("New added link(2) not returned by getLinksTo():" + link2, hasLink2);
//
//                VFSPath linkFile1 = getRemoteTestDir().openVFSPath(link1name);
//                VFSPath linkFile2 = getRemoteTestDir().openVFSPath(link1name);
//
//                Assert.assertTrue("Created link must exist:" + linkFile1, linkFile1.exists());
//                Assert.assertTrue("Created link must exist:" + linkFile2, linkFile2.exists());
//
//                if (linkFile1 instanceof VLogicalFileAlias)
//                    Assert.assertTrue("Created link must report it is an Alias:" + linkFile1,
//                            ((VLogicalFileAlias) linkFile1).isAlias());
//                else
//                    Assert.fail("Created linkfile (or alias) doesn' seem to support the VLogicalFileAlias!:"
//                            + linkFile1);
//
//                if (linkFile2 instanceof VLogicalFileAlias)
//                    Assert.assertTrue("Created link must report it is an Alias:" + linkFile2,
//                            ((VLogicalFileAlias) linkFile2).isAlias());
//                else
//                    Assert.fail("Created linkfile (or alias) doesn' seem to support the VLogicalFileAlias!:"
//                            + linkFile2);
//            }
//            else
//            {
//                message("skipping VLogicalFileAlias tests for:" + orgFile);
//            }
//        }
//        finally
//        {
//            message("Deleting:" + orgFile);
//            orgFile.delete();
//
//            message("Deleting:" + link1);
//            if (existsFile(getRemoteTestDir(),link1name))
//                getRemoteTestDir().deleteFile(link1name);
//
//            message("Deleting:" + link2);
//            if (existsFile(getRemoteTestDir(),link2name))
//                getRemoteTestDir().deleteFile(link2name);
//        }
//    }
//
//   // ========================================================================
//    // Test eXtra Resource Interfaces (Under construction)
//    // ========================================================================
//    
//    @Test public void testPosixAttributes() throws Exception
//    {
//    	if ((getRemoteTestDir().isLocal()) && (GlobalProperties.isWindows()))
//    	{
//    		message("Skipping Unix atributes test for windows filesystem...");
//    		return; 
//    	}
//
//        VFSPath remoteFile = createRemoteFile("testUnixAttrs.txt");
//
//
//        if (remoteFile instanceof VUnixFileAttributes)
//        {
//            VUnixFileAttributes uxFile = (VUnixFileAttributes) remoteFile;
//
//            // write something.
//            writeContents(remoteFile,TEST_CONTENTS);
//
//            String uid = uxFile.getUid();
//            if (StringUtil.isEmpty(uid))
//                Assert.fail("User ID (UID) is empty for (VUnixFileAttributes).getUid():" + remoteFile);
//
//            String gid = uxFile.getGid();
//            if (StringUtil.isEmpty(gid))
//                Assert.fail("Group ID (GID) is empty for (VUnixFileAttributes).getGid():" + remoteFile);
//
//            int mode = uxFile.getMode();
//            // mode==0 is allowed!
//            if (mode < 0)
//                Assert.fail("Unix File mode is negative for:" + remoteFile);
//        }
//
//        if (remoteFile.exists())
//            remoteFile.delete();
//    }
//
//    @Test public void testVChecksum() throws Exception
//    {
//        VFSPath remoteFile = createRemoteFile("testChecksum.txt");
//        writeContents(remoteFile,TEST_CONTENTS);
//
//        if (remoteFile instanceof VChecksum)
//        {
//
//            VChecksum checksumRemoteFile = (VChecksum) remoteFile;
//           
//            String[] types = checksumRemoteFile.getChecksumTypes();
//
//            String calculated;
//            String fetched;
//            for (int i = 0; i < types.length; i++)
//            {
//                // recreate InputStream:
//                InputStream remoteIn = remoteFile.createInputStream();
//                
//                message("Testing checksum type:" + types[i]);
//                // check if both methods retun the same checksum
//                calculated = ChecksumUtil.calculateChecksum(remoteIn, types[i]);
//                fetched = checksumRemoteFile.getChecksum(types[i]);
//                message(" -> calculated Checksum:" + calculated + " fetched Checksum:" + fetched);
//                Assert.assertEquals("Wrong checksum",calculated, fetched);
//
//                // now change the file and check if the checksum has also
//                // chanded
//                String initialChecksum = calculated;
//                writeContents(remoteFile,"Changed contents");
//                remoteFile = getRemoteTestDir().openVFSPath("testChecksum.txt");
//
//                String newFetched = checksumRemoteFile.getChecksum(types[i]);
//                message(" -> Updated: new checksum:"+newFetched);
//                Assert.assertNotSame("New checksum should be different (type="+types[i]+")",newFetched, initialChecksum);
//
//                // download the file and compare it against the remote
//                VFSPath result = remoteFile.copyTo(localTempDir); 
//                LFile localFile = (LFile) result;
//
//                if (localFile instanceof VChecksum)
//                {
//                    VChecksum checksumLocalFile = (VChecksum) localFile;
//                    checksumRemoteFile = (VChecksum) remoteFile;
//
//                    message(" -> checking checksum type:" + types[i] + " from local file");
//                    String localChecksum = checksumLocalFile.getChecksum(types[i]);
//                    message(" -> checking checksum type:" + types[i] + " from remote file");
//                    String remoteChecksum = checksumRemoteFile.getChecksum(types[i]);
//
//                    message(" - > localChecksum: " + localChecksum + " remoteChecksum: " + remoteChecksum);
//                    Assert.assertEquals(localChecksum, remoteChecksum);
//                }
//                // Check exception
//                try
//                {
//                    String remoteChecksum = checksumRemoteFile.getChecksum("NON EXISTING ALGORITHM");
//                    message(" -> *** Got invalid checksum:" + remoteChecksum);
//                }
//                catch (Exception ex)
//                {
//                    if (!(ex instanceof nl.esciencecenter.vlet.exception.NotImplementedException))
//                    {
//                        Assert.fail("Should throw NotImplementedException. Instead got back " + ex.getMessage());
//                    }
//                    else
//                    {
//                        message(" -> Correct exeption!!: " + ex.getMessage());
//                    }
//                }
//                localFile.delete();
//            }
//        }
//    }
//
//    @Test public void testVReplicatable() throws Exception
//    {
//
//        String fileName = "testReplicable.txt";
//        VFSPath remoteFile = null;
//
//        // if file has no replic create one
//        if (!existsFile(getRemoteTestDir(),fileName))
//        {
//            remoteFile = createRemoteFile(fileName);
//            writeContents(remoteFile,"Test contents");
//        }
//        else
//        {
//            remoteFile = getRemoteTestDir().openVFSPath(fileName);
//        }
//
//        if ((remoteFile instanceof VReplicatable) == false)
//        {
//            // delete?
//            return;
//        }
//        VReplicatable replicable = (VReplicatable) remoteFile;
//        long len = remoteFile.fileLength();
//
//        VRL[] replicas = replicable.getReplicas();
//        ITaskMonitor monitor = getVRSContext().getTaskWatcher().getCurrentThreadTaskMonitor("Test VReplicable:", -1);
//
//        // Keep only one replica
//        for (int i = 0; i < replicas.length; i++)
//        {
//            message("Registered replica VRLs: " + replicas[i]);
//            if (i >= 1)
//            {
//                replicable.deleteReplica(monitor, replicas[i].getHostname());
//            }
//        }
//
//        BdiiService service = BdiiUtil.getBdiiService(VRSContext.getDefault()); 
//        
//        // get all se
//        ArrayList<StorageArea> se = service.getSRMv22SAsforVO(VRSContext.getDefault().getVO());
//
//        VRSClient vfs = getVFS(); 
//        
//
//        unregisterEmptyRep(replicable, vfs);
//
//        VRL replicaVRL;
//        VFSPath replicaFile;
//        boolean success = false;
//        StringList blackListedSEs = new StringList(TestSettings.BLACK_LISTED_SE);
//
//        // replacate to all except rug
//        for (int i = 0; i < se.size(); i++)
//        // for (int i = 0; i < 3; i++)
//        {
//            String host = se.get(i).getHostname();
//
//            // skip original replica
//            if (se.get(i).getHostname().equals(replicas[0].getHostname()))
//                continue;
//
//            if (blackListedSEs.contains(host) == false)
//            {
//                message("Storage Element: " + se.get(i).getHostname());
//                message("   Replicating");
//                replicaVRL = replicable.replicateTo(monitor, se.get(i).getHostname());
//                replicaFile = vfs.openVFSPath(replicaVRL);
//                message("   Replecated to: " + replicaFile);
//
//                // ----Check if it is correctly replicated-------------
//                // is file created ??
//                boolean exists = replicaFile.exists();
//                message("   Exists?: " + exists);
//                Assert.assertTrue(exists);
//                // is the same size??
//                long replicaLen = replicaFile.fileLength();
//                message("   Original length " + len + " replica lenght: " + replicaLen);
//                Assert.assertEquals(len, replicaLen);
//
//                // checksum
//                if ((replicaFile instanceof VChecksum) && (remoteFile instanceof VChecksum))
//                {
//                    String replicaChecksome = ((VChecksum) replicaFile).getChecksum(VChecksum.MD5);
//                    String remoteFileChecksome = ((VChecksum) remoteFile).getChecksum(VChecksum.MD5);
//                    Assert.assertEquals(remoteFileChecksome, replicaChecksome);
//                }
//
//                // -----------------------DELETE----------------------------
//                message("   Deleteing");
//                success = replicable.deleteReplica(monitor, se.get(i).getHostname());
//                Assert.assertTrue(success);
//                exists = replicaFile.exists();
//                message("   Exists?: " + exists);
//                Assert.assertFalse(exists);
//            }
//            else
//            {
//                message("Ooops it's " + se.get(i).getHostname() + " don't replicate there");
//            }
//        }
//
//        // test Exception
//        try
//        {
//            replicaVRL = replicable.replicateTo(monitor, "NON-EXISTING-SE");
//            message("New replica!!: " + replicaVRL);
//            Assert.fail("Replicating to UNKNOWN storage element should fail");
//        }
//        catch (Exception ex)
//        {
//            if (!(ex instanceof ResourceCreationException))
//            {
//                Assert.fail("Wrong exeption. Should be ResourceCreationFailedException instead got: " + ex);
//            }
//            else
//            {
//                message("Got back correct exception: " + ex);
//            }
//        }
//
//        // get back the replicas VRL
//        replicas = replicable.getReplicas();
//
//        message("Listing replicas.........");
//        for (int i = 0; i < replicas.length; i++)
//        {
//            message("Registered replica VRLs: " + replicas[i]);
//        }
//
//        // Test Register
//        int VRllen = 4;
//        VRL[] vrls = new VRL[VRllen];
//        // warning!!! if you include port number LFC will remove it
//        for (int i = 0; i < VRllen; i++)
//        {
//            vrls[i] = new VRL("scheme://host.at.some.domain/path/to/file" + i);
//        }
//
//        success = replicable.registerReplicas(vrls);
//        Assert.assertTrue(success);
//
//        // get back the replicas VRL
//        replicas = replicable.getReplicas();
//        List<VRL> vrlList = Arrays.asList(replicas);
//
//        message("Listing replicas.........");
//        for (int i = 0; i < replicas.length; i++)
//        {
//            message("Registered replica VRLs: " + replicas[i]);
//            if (i < vrls.length)
//            {
//                // message("Is " + vrls[i] + " contained in vrlList?");
//                // check if registered vrls are there
//                if (!vrlList.contains(vrls[i]))
//                {
//                    Assert.fail("Didn't get back the same VRLs from the service. " + replicas[i]
//                            + " is not contained in the registered VRS");
//                }
//            }
//
//        }
//
//        message("Unregistering replicas.........");
//        success = replicable.unregisterReplicas(vrls);
//        Assert.assertTrue(success);
//
//        replicas = replicable.getReplicas();
//        vrlList = Arrays.asList(replicas);
//
//        message("Listing replicas.........");
//        for (int i = 0; i < replicas.length; i++)
//        {
//            message("Registered replica VRLs: " + replicas[i]);
//            if (i < vrls.length)
//            {
//                // check if unregistered vrls are gone
//                if (vrlList.contains(vrls[i]))
//                {
//                    Assert.fail("Didn't remove VRLs. " + replicas[i] + " is contained in the registered VRS");
//                }
//            }
//
//        }
//        // clean up
//        success = remoteFile.delete();
//        Assert.assertTrue(success);
//
//    }
//
//    private void unregisterEmptyRep(VReplicatable rep, VRSClient vfs)
//    {
//        VRL[] replicas = null;
//        if (vfs == null)
//        {
//            vfs = getVFS();
//        }
//        VFSPath file = null;
//        ArrayList<VRL> emptyRep = new ArrayList<VRL>();
//        try
//        {
//            replicas = rep.getReplicas();
//        }
//        catch (Exception e)
//        {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//
//        message("Listing replicas.........");
//        for (int i = 0; i < replicas.length; i++)
//        {
//
//            try
//            {
//                // file = vfs.resolvePath(replicas[i]);
//                file = vfs.openVFSPath(replicas[i]);
//                if (!file.exists())
//                {
//                    message("Replica VRL: " + replicas[i] + " doesn't exist. Unregistering");
//                    emptyRep.add(replicas[i]);
//                }
//            }
//            catch (Exception e)
//            {
//                message("Replica VRL: " + replicas[i] + " doesn't exist. Unregistering");
//                emptyRep.add(replicas[i]);
//            }
//        }
//
//        VRL[] emptyRepArray = new VRL[emptyRep.size()];
//        emptyRepArray = emptyRep.toArray(emptyRepArray);
//        try
//        {
//            rep.unregisterReplicas(emptyRepArray);
//            replicas = rep.getReplicas();
//        }
//        catch (Exception e)
//        {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//
//        message("Listing replicas.........\n");
//        for (int i = 0; i < replicas.length; i++)
//        {
//            message("Remaing replicas: " + replicas[i]);
//        }
//
//    }
//
//   
//    // ========================================================================
//    // Explicit Regression Tests (Start with 'Z' to show up last in eclipse)
//    // ========================================================================
//
//    /**
//     * Test whether LFC file with zero size, but non zero replicas still can be
//     * copied and downloaded.
//     */
//    @Test public void testZRegressionLFCZeroFileSize() throws Exception
//    {
////        VFSPath remoteDir = getRemoteTestDir();
////
////        // Skip non LFC files:
////        if ((remoteDir instanceof LFCDir) == false)
////            return;
////
////        LFCFile lfcFile = (LFCFile) createRemoteFile(nextFilename("lfcZeroLengthFile"), true);
////
////        String orgStr = "Test Contents";
////        lfcFile.setContents(orgStr);
////        long orgLen = lfcFile.getLength();
////        lfcFile.updateFileSize(0);
////
////        long newSize = lfcFile.getLength();
////        Assert.assertEquals("After setFileSize(): LFC File size should be zero", 0, newSize);
////
////        LFCFile lfcTarget = (LFCFile) createRemoteFile(nextFilename("lfcTarget"));
////
////        if (lfcTarget.exists())
////            lfcTarget.delete();
////
////        lfcTarget = (LFCFile) lfcFile.copyTo((VFSPath) lfcTarget);
////
////        String newStr = lfcTarget.getContentsAsString();
////
////        Assert.assertEquals("When copying an LFC File with the wrong size, the new file should have the correct size",
////                orgLen, lfcTarget.getLength());
////
////        // ===
////        // Checks:
////        // ===
////        if (StringUtil.compare(orgStr, newStr) != 0)
////        {
////            Assert
////                    .fail("Regression Failure: Although the LFC file size is zero,the contents of the new file should be the original content. Contents='"
////                            + newStr + "'");
////        }
////
////        VFSPath localFile = lfcTarget.copyToDir(this.getLocalTempDirVRL());
////        Assert.assertEquals(
////                "Aftger downloading an LFC File with the wrong size, the local file should have the correct size",
////                orgLen, localFile.getLength());
////
////        try
////        {
////            localFile.delete();
////        }
////        catch (Exception e)
////        {
////        }
////        try
////        {
////            lfcTarget.delete();
////        }
////        catch (Exception e)
////        {
////        }
////        try
////        {
////            lfcFile.delete();
////        }
////        catch (Exception e)
////        {
////        }
//    }
//
//    /**
//     * Test whether LFC file with corrupted Replica information
//     * can be deleted using force delete. 
//     */
//    @Test public void testZForceDeleteCorruptedLFCFile() throws Exception
//    {
////        VFSPath remoteDir = getRemoteTestDir();
////
////        // Skip non LFC files:
////        if ((remoteDir instanceof LFCDir) == false)
////            return;
////
////        LFCFile lfcFile = (LFCFile) createRemoteFile(nextFilename("lfcForceDeleteFile1"), true);
////
////        // create at least one valid replica. 
////        String orgStr = "Test Contents";
////        lfcFile.setContents(orgStr);
////
////        // create faulty replica, SRM will throw connection exception !
////        VRL vrls[]=new VRL[1]; 
////        vrls[0]=new VRL("srm","localhost",8443,"/dummy/path/to/replica"); 
////        lfcFile.registerReplicas(vrls);
////        
////        try
////        {
////            // should throw error as faulty replica can't be deleted. 
////            lfcFile.delete();
////            Assert.fail("Deleting a corrupted LFC file is only possible using 'forceDelete'"); 
////        }
////        catch (Exception e)
////        {
////            message("Force delete test: Caught expected exception:"+e);     
////        }
////
////        // use LFCClient's forceDelete
////        LFCClient lfcClient = lfcFile.getLFCClient();
////        lfcClient.recurseDelete(lfcFile,true); 
////        Assert.assertFalse("LFC File must always be deleted after force delete.",lfcFile.exists()); 
//     
//    }
//    /**
//     * Regression test for SRM to check whether default storage type is
//     * PERMANENT.
//     * 
//     */
//    @Test public void testZRegressionSRMStorageType() throws Exception
//    {
////        VFSPath remoteDir = getRemoteTestDir();
////
////        if (remoteDir.getScheme().compareToIgnoreCase(VRS.SRM_SCHEME) == 0)
////        {
////            VFSPath newFile = createRemoteFile(nextFilename("testFile"), true);
////
////            VAttribute attr = newFile.getAttribute(SRMConstants.ATTR_SRM_STORAGE_TYPE);
////
////            Assert.assertNotNull("SRM File must have storage type attribute:" + SRMConstants.ATTR_SRM_STORAGE_TYPE,
////                    attr);
////            Assert.assertNotNull("SRM File must have storage type attribute:" + SRMConstants.ATTR_SRM_STORAGE_TYPE,
////                    attr.getValue());
////            Assert.assertEquals("SRM File must have default PERMANENT storage type attribute.",
////                    SRMConstants.STORAGE_TYPE_PERMANENT, attr.getValue());
////
////            try
////            {
////                newFile.delete();
////            }
////            catch (Exception e)
////            {
////                debug(" Exception when deleting:" + newFile);
////            }
////        }
//
//    }
//
//
//    
//    
//    
//    
//    
//    
//    
//    
//    
//    
//    
//    
//    
//    
//   
// 
//
//}

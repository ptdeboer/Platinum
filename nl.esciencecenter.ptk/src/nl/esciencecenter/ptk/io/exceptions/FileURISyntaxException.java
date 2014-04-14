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

package nl.esciencecenter.ptk.io.exceptions;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Wrapper for nested URI Syntax Exceptions on File Locations.
 */
public class FileURISyntaxException extends IOException
{
    private static final long serialVersionUID = -5950527403084197333L;

    protected String fileLocation = null;

    public FileURISyntaxException(String message, String location)
    {
        super(message);
        this.fileLocation = location;
    }

    public FileURISyntaxException(String message, String location, URISyntaxException cause)
    {
        super(message, cause);
        this.fileLocation = location;
    }

    public String getFileLocation()
    {
        return fileLocation;
    }

    public String getInput()
    {
        Throwable orgCause = this.getCause();

        if (this.getCause() instanceof URISyntaxException)
        {
            return ((URISyntaxException) orgCause).getInput();
        }
        return null;
    }

}

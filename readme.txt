Module description:
The ddieditor is the base API for CRUD operations on instances of the DDI 3.x
specification.

Legal:
Copyright 2009 Danish Data Archive (http://www.dda.dk)

For all files within ddieditor:

Ddieditor is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Ddieditor is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser Public License for more details.

You should have received a copy of the GNU Lesser Public License
along with ddieditor. If not, see <http://www.gnu.org/licenses/>.

Building:
The project uses an ANT build file
Buildfile: build.xml

Notice: Currently the project only builds when it and its dependencies are setup
within an Eclipse enviroment.

Setup:
1. The module 'lib-java' holding needed jars needs to be checked out from source 
   version control as weel at: ../[project_home]
2. The module 'ddi-3-xmlbeans' holding the XmlBeans jars needs to be checked
   out from source version control as weel at: ../[project_home]
3. The module 'util' holding utilities needs to be checked out from source 
   version control as weel at: ../[project_home]   
4. The module needs internet access to checkout the DDI schemas from the DDI 
   source version control at SourgeForge

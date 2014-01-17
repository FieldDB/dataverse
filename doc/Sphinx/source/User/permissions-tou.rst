Permissions and Terms of Use
++++++++++++++++++++++++++++++


Edit Terms for Study Creation
=============================

You can set up Terms of Use for the dataverse that require users to
acknowledge your terms and click "Accept" before they can contribute to
the dataverse.

Navigate to the Terms for Study Creation from the Options page:

``Dataverse home page > Options page > Permissions tab > Terms subtab > Deposit Terms of Use``

To set Terms of Use for creating or uploading to the dataverse:

#. Click the Enable Terms of Use check box.
#. Enter a description of your terms to which visitors must agree before
   they can create a study or upload a file to an existing study.
   Note: A light blue background in any form field indicates HTML,
   JavaScript, and style tags are permitted. The ``html`` and ``body``
   element types are not allowed.

Edit Terms for File Download
============================

You can set up Terms of Use for the network that require users to
acknowledge your terms and click "Accept" before they can download or
subset contents from the network.

Navigate to the Terms for File Download from the Options page:

``Dataverse home page > Options page > Permissions tab > Terms subtab > Download Terms of Use``

To set Terms of Use for downloading or subsetting contents from any
dataverse in the network:

#. Click the Enable Terms of Use check box.
#. Enter a description of your terms to which visitors must agree before
   they can download or analyze any file.
   Note: A light blue background in any form field indicates HTML,
   JavaScript, and style tags are permitted. The ``html`` and ``body``
   element types are not allowed.

Manage Permissions
==================

Enable contribution invitation, grant permissions to users and groups,
and manage dataverse file permissions.

Navigate to Manage Permissions from the Options page:

``Dataverse home page > Options page > Permissions tab > Permissions subtab``

**Contribution Settings**

Choose the access level contributors have to your dataverse. Whether
they are allowed to edit only their own studies, all studies, or whether
all registered users can edit their own studies (Open dataverse) or all
studies (Wiki dataverse). In an Open dataverse, users can add studies by
simply creating an account, and can edit their own studies any time,
even after the study is released. In a Wiki dataverse, users cannot only
add studies by creating an account, but also edit any study in that
dataverse. Contributors cannot, however, release a study directly. After
their edits, they submit it for review and a dataverse administrator or
curator will release it.

**User Permission Settings**

There are several roles defined for users of a Dataverse Network
installation:

-  Data Users - Download and analyze all types of data
-  Contributors - Distribute data and receive recognition and citations
   to it
-  Curators - Summarize related data, organize data, or manage multiple
   sets of data
-  Administrators - Set up and manage contributions to your dataverse,
   manage the appearance of your dataverse, organize your dataverse
   collections

**Privileged Groups**

Enter group name to allow a group access to the dataverse. Groups are
created by network administrators.

**Dataverse File Permission Settings**

Choose 'Yes' to restrict ALL files in this dataverse. To restrict files
individually, go to the Study Permissions page of the study containing
the file.

**Role/State Table**

+---------------------+-----------+----------------+------------------+------------------+---------------------+
|                     | **Role**  |                |                  |                  |                     |
+=====================+===========+================+==================+==================+=====================+
| **Version State**   | None      | Contributor +, | Curator          | Admin            | Network Admin**     |
|                     |           | ++             |                  |                  |                     |
+---------------------+-----------+----------------+------------------+------------------+---------------------+
| Draft               |           | E,E2,D3,S,V    | E,E2,P,T,D3,R,V  | E,E2,P,T,D3,R,V  | E,E2,P,T,D3,D2,R,V  |
+---------------------+-----------+----------------+---+--------------+------------------+---------------------+
| In Review           |           | E,E2,D3,V      | E,E2,P,T,D3,R,V  | E,E2,P,T,D3,R,V  | E,E2,P,T,D3,R,D2,V  |
+---------------------+-----------+----------------+------------------+------------------+---------------------+
| Released            |  V        | E,V            | E,P,T,D1,V       | E,P,T,D1,V       | E,P,T,D2,D1,V       |
+---------------------+-----------+----------------+------------------+------------------+---------------------+
|  Archived           |  V        | V              | P,T,V            | P,T,V            | P,T,D2,V            |
+---------------------+-----------+----------------+------------------+------------------+---------------------+
|  Deaccessioned      |           |                | P,T,R2,V         | P,T,R2,V         | P,T,R2,D2,V         |
+---------------------+-----------+----------------+------------------+------------------+---------------------+


**Legend:**

E = Edit (Cataloging info, File meta data, Add files)

E2 = Edit Study Version Notes

D1 = Deaccession

P = Permission

T = Create Template

D2 = Destroy

D3 = Delete Draft, Delete Review Version

S = Submit for Review

R = Release

R2 = Restore

V = View

 

**Notes:**

*\Same as Curator

**\Same as Curator + D2

+\ Contributor actions (E,D3,S,V) depend on new DV permission settings. A
contributor role can act on their own studies (default) or all studies
in a dv, and registered users can become contributors and act on their
own studies or all studies in a dv.

++ A contributor is defined either as a contributor role or as any
registered user in a DV that allows all registered users to contribute.

 

# Manage External Guidance Frontend

This repo models a process allowing external guidance authors, approvers and publishers to make guidance available to the public. The service is provided in the English language.


### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").

## Endpoints

### `POST: /external-guidance/process/scratch`

#### Purpose

Allows Ocelot to send over a scratch process and check how it looks in the browser.
The generated ID is returned to the caller, with a location header enabling the user to be redirected to view the submitted process.

#### Request Body

This is the json string representing the Ocelot process

#### Success Response

**HTTP Status**: `201`

**Headers**:
```
location -> "/guidance/scratch/:id"
```

**Example Response Body**:
```
{
   "id": "265e0178-cbe1-42ab-8418-7120ce6d0925"
}
```

#### Error Responses

**Error response format**
```
{
   "code": "ERROR_CODE",
   "message": "Human readable error message"
}
```

<table>
    <thead>
        <tr>
            <th>HTTP Status</th>
            <th>Error Code</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td><pre>400</pre></td>
            <td><pre>BAD_REQUEST</pre></td>
        </tr>
        <tr>
            <td><pre>500</pre></td>
            <td><pre>INTERNAL_SERVER_ERROR</pre></td>
        </tr>
    </tbody>
</table>

### `POST: /external-guidance/process/approval/fact-check`

#### Purpose

Allows Ocelot to send over a process and make it available for Fact Checking
The ocelot ID is returned to the caller.

#### Request Body

This is the json string representing the Ocelot process

#### Success Response

**HTTP Status**: `201`

**Example Response Body**:
```
{
   "id": "oct90005"
}
```

#### Error Responses

**Error response format**
```
{
   "code": "ERROR_CODE",
   "message": "Human readable error message"
}
```

<table>
    <thead>
        <tr>
            <th>HTTP Status</th>
            <th>Error Code</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td><pre>400</pre></td>
            <td><pre>BAD_REQUEST</pre></td>
        </tr>
        <tr>
            <td><pre>500</pre></td>
            <td><pre>INTERNAL_SERVER_ERROR</pre></td>
        </tr>
    </tbody>
</table>

### `POST: /external-guidance/process/approval/2i-review`

#### Purpose

Allows Ocelot to send over a process and make it available for a 2i Review
The ocelot ID is returned to the caller.

#### Request Body

This is the json string representing the Ocelot process

#### Success Response

**HTTP Status**: `201`

**Example Response Body**:
```
{
   "id": "oct90005"
}
```

#### Error Responses

**Error response format**
```
{
   "code": "ERROR_CODE",
   "message": "Human readable error message"
}
```

<table>
    <thead>
        <tr>
            <th>HTTP Status</th>
            <th>Error Code</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td><pre>400</pre></td>
            <td><pre>BAD_REQUEST</pre></td>
        </tr>
        <tr>
            <td><pre>500</pre></td>
            <td><pre>INTERNAL_SERVER_ERROR</pre></td>
        </tr>
    </tbody>
</table>


### All tests and checks

> `sbt runAllChecks`

This is an sbt command alias specific to this project. It will run
- unit tests
- integration tests
- and produce a coverage report.

You can view the coverage report in the browser by pasting the generated url.

#### Installing sbt plugin to check for library updates.
To check for dependency updates locally you will need to create this file locally ~/.sbt/1.0/plugins/sbt-updates.sbt
and paste - addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.6.3") - into the file.
Then run:

> `sbt dependencyUpdates `

To view library update suggestions - this does not cover sbt plugins.
It is not advised to install the plugin for the project.


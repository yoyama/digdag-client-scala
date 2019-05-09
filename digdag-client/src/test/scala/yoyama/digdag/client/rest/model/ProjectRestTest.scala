package yoyama.digdag.client.rest.model

import org.scalatest.{FlatSpec, Matchers}

import scala.util.Try

class ProjectRestTest  extends FlatSpec with Matchers {

  "Valid json" should "parsed" in {
    val data = """ { "projects" : [
            { "id":"1",
              "name":"test-proj1",
              "revision":"47b78f24-7195-458c-b91a-b3fd059aba2e",
              "createdAt":"2019-04-30T14:24:38Z",
              "updatedAt":"2019-04-30T14:24:38Z",
              "deletedAt":null,
              "archiveType":"db",
              "archiveMd5":"cCkGbCesb17xjWYNV0GXmg=="
            } ] }
    """.stripMargin
    val projects: Try[Seq[ProjectRest]] = ProjectRest.toProjects(data)
    println(projects)
    assert(projects.isSuccess)
  }

  "Invalid json1" should "be error" in {
    val data = """ { "proj" : [
            { "id":"1",
              "name":"test-proj1",
              "revision":"47b78f24-7195-458c-b91a-b3fd059aba2e",
              "createdAt":"2019-04-30T14:24:38Z",
              "updatedAt":"2019-04-30T14:24:38Z",
              "deletedAt":null,
              "archiveType":"db",
              "archiveMd5":"cCkGbCesb17xjWYNV0GXmg=="
            } ] }
    """.stripMargin
    val projects: Try[Seq[ProjectRest]] = ProjectRest.toProjects(data)
    println(projects)
    assert(projects.isFailure)
  }

  "Invalid json2" should "be error" in {
    val data = """ { "projects" : [
            { "id":"1",
              "revision":"47b78f24-7195-458c-b91a-b3fd059aba2e",
              "createdAt":"2019-04-30T14:24:38Z",
              "updatedAt":"2019-04-30T14:24:38Z",
              "deletedAt":null,
              "archiveType":"db",
              "archiveMd5":"cCkGbCesb17xjWYNV0GXmg=="
            } ] }
    """.stripMargin
    val projects: Try[Seq[ProjectRest]] = ProjectRest.toProjects(data)
    println(projects)
    assert(projects.isFailure)
  }

}

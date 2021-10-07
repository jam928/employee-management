package com.tm.employeemanagement.model

import java.io.Serializable
import javax.persistence.*

@Entity
class Employee: Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false, updatable = false)
    var id: Long? = null
    var name: String? = null
    var email: String? = null
    var jobTitle: String? = null
    var phone: String? = null
    var imageUrl: String? = null
    @Column(nullable = false, updatable = false)
    var employeeCode: String? = null
}
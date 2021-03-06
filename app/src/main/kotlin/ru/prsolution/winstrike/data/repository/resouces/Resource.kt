package ru.prsolution.winstrike.data.repository.resouces

data class Resource<out T> constructor(
        val state: ResourceState,
        val data: T? = null,
        val message: String? = null
)

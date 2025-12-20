package command

interface ICommandBuilder {
    fun build(): Result<String> // success or failure message
}
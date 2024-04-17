package com.akree.expensetracker.fragments

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.akree.expensetracker.AuthorizationActivity
import com.akree.expensetracker.R
import com.akree.expensetracker.databinding.FragmentProfileBinding
import com.akree.expensetracker.models.ExpensesViewModel
import com.akree.expensetracker.models.ProfileViewModel
import com.akree.expensetracker.serialization.User
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import io.getstream.avatarview.coil.loadImage
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.util.*


class ProfileFragment : Fragment() {
    private var binding: FragmentProfileBinding? = null

    private var viewModel: ProfileViewModel? = null
    private var expensesModel: ExpensesViewModel? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
        expensesModel = ViewModelProvider(this)[ExpensesViewModel::class.java]

        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel!!.user.observe(viewLifecycleOwner) { user -> updateDataFromUser(user) }
        updateDataFromUser(viewModel!!.user.value)

        connectActionHandlers()

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun updateDataFromUser(user: User?) {
        if (user != null) {
            binding!!.pfUserNameMsg.text = user.username
            binding!!.pfUserEmailMsg.text = user.email

            val nameParts = user.username.split("\"\\\\s+\"").toTypedArray()
            val nameInitials = StringBuilder()
            for (part in nameParts) {
                nameInitials.append(part[0])
            }
            binding!!.pfAvatarView.avatarInitials = nameInitials.toString().uppercase(Locale.getDefault())

            if (user.profilePicture.isNotEmpty()) {
                FirebaseStorage.getInstance()
                        .reference.child(user.profilePicture)
                        .downloadUrl.addOnSuccessListener {
                            binding!!.pfAvatarView.avatarInitials = ""
                            binding!!.pfAvatarView.loadImage(it)
                        }
            }

            binding!!.pfCategoriesGroup.removeAllViews()
            for (category in user.categories) {
                val chip = LayoutInflater.from(requireContext())
                        .inflate(
                                R.layout.layout_chip_entry,
                                binding!!.pfCategoriesGroup,
                                false
                        ) as Chip

                chip.text = category
                chip.isCheckable = false

                chip.setOnCloseIconClickListener {
                    val myself: Chip = it as Chip
                    val categories = viewModel!!.user.value?.categories

                    categories?.remove(myself.text.toString())

                    FirebaseDatabase.getInstance()
                            .getReference("user/" + FirebaseAuth.getInstance().currentUser!!.uid + "/categories")
                            .setValue(categories)
                }

                binding!!.pfCategoriesGroup?.addView(chip)
            }
        }
    }

    private fun connectActionHandlers() {
        binding?.pfLogoutBtn!!.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            startActivity(Intent(activity, AuthorizationActivity::class.java))
            activity?.finish()
        }

        binding?.pfAvatarView!!.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_PICK

            resultLauncher.launch(intent)
        }

        binding?.pfAddCategoryBtn!!.setOnClickListener {
            val dialogBuilder = android.app.AlertDialog.Builder(requireContext())
            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.add_category_dialog, null)
            dialogBuilder.setView(dialogView)

            val dialog = dialogBuilder.create()

            val addBtn = dialogView.findViewById<Button>(R.id.button_add_c)
            addBtn.setOnClickListener {
                val categoryName = dialogView.findViewById<EditText>(R.id.category_name_et).text.toString()

                if (categoryName.isNotEmpty()) {
                    val categoryList = viewModel?.user?.value!!.categories
                    categoryList.add(categoryName)

                    FirebaseDatabase.getInstance()
                            .getReference("user/" + FirebaseAuth.getInstance().currentUser!!.uid + "/categories")
                            .setValue(categoryList)
                }

                dialog.dismiss()
            }

            dialog.show()
        }

        binding?.pfExportCsvBtn?.setOnClickListener {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)

            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "text/csv"
            intent.putExtra(Intent.EXTRA_TITLE, "data.csv")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, Uri.parse("/Documents"))

            csvExportLauncher.launch(intent)
        }

        binding?.pfExportXlsBtn?.setOnClickListener {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)

            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            intent.putExtra(Intent.EXTRA_TITLE, "data.xlsx")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, Uri.parse("/Documents"))

            xlsExportLauncher.launch(intent)
        }
    }

    private val csvExportLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = result!!.data!!.data
            val outputFile = context?.contentResolver!!
                .openFileDescriptor(uri!!, "w")

            if (outputFile != null) {
                val outputStream = BufferedOutputStream(FileOutputStream(outputFile.fileDescriptor))

                outputStream.write("Type,Date,Category,Amount\n".toByteArray())

                for (expense in expensesModel!!.expenses!!.value!!.values) {
                    outputStream.write((expense.type + ",").toByteArray())
                    outputStream.write((expense.date + ",").toByteArray())
                    outputStream.write((expense.category + ",").toByteArray())
                    outputStream.write((expense.amount.toString() + "\n").toByteArray())
                }

                outputStream.flush()
                outputStream.close()

                Toast.makeText(
                    requireContext(),
                    "Exported successfully",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private val xlsExportLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = result!!.data!!.data
            val outputFile = context?.contentResolver!!
                .openFileDescriptor(uri!!, "w")

            if (outputFile != null) {
                val workbook = XSSFWorkbook()
                val sheet = workbook.createSheet("Account")

                for (i in 0..3) sheet.setColumnWidth(i, 3000)

                val header = sheet.createRow(0)

                val headerStyle: CellStyle = workbook.createCellStyle()
                headerStyle.fillForegroundColor = IndexedColors.LIGHT_BLUE.getIndex()
                headerStyle.fillPattern = FillPatternType.SOLID_FOREGROUND

                val font = workbook.createFont()
                font.fontName = "Arial"
                font.fontHeightInPoints = 16.toShort()
                font.bold = true
                headerStyle.setFont(font)

                for ((ind, el) in arrayListOf("Type", "Date", "Category", " Amount").withIndex()) {
                    val headerCell: Cell = header.createCell(ind)
                    headerCell.setCellValue(el)
                    headerCell.cellStyle = headerStyle
                }

                for ((ind, el) in expensesModel!!.expenses!!.value!!.values.withIndex()) {
                    val row = sheet.createRow(ind + 1)

                    var cell = row.createCell(0)
                    cell.setCellValue(el.type)

                    cell = row.createCell(1)
                    cell.setCellValue(el.date)

                    cell = row.createCell(2)
                    cell.setCellValue(el.category)

                    cell = row.createCell(3)
                    cell.setCellValue(el.amount)
                }

                val outputStream = BufferedOutputStream(FileOutputStream(outputFile.fileDescriptor))

                workbook.write(outputStream)
                workbook.close()

                outputStream.flush()
                outputStream.close()

                Toast.makeText(
                    requireContext(),
                    "Exported successfully",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = result?.data?.data
            val inputStream = context?.contentResolver?.openInputStream(uri!!)

            try {
                val bitmap = BitmapFactory.decodeStream(inputStream)

                binding?.pfAvatarView?.avatarInitials = ""
                binding!!.pfAvatarView.loadImage(bitmap)

                val link = "images/" + UUID.randomUUID()

                FirebaseStorage.getInstance()
                        .reference.child(link)
                        .putFile(uri!!).addOnSuccessListener { snapshot ->
                            snapshot.storage.downloadUrl.addOnSuccessListener {
                                FirebaseDatabase.getInstance()
                                        .getReference("user/" + FirebaseAuth.getInstance().currentUser!!.uid + "/profilePicture")
                                        .setValue(link).addOnSuccessListener {
                                            Toast.makeText(
                                                    context,
                                                    "Uploading completed",
                                                    Toast.LENGTH_LONG
                                            ).show()
                                        }.addOnFailureListener {
                                            Toast.makeText(
                                                    context,
                                                    "Uploading failed",
                                                    Toast.LENGTH_LONG
                                            ).show()
                                        }
                            }
                        }.addOnFailureListener {
                            Toast.makeText(
                                    context,
                                    "Something went wrong",
                                    Toast.LENGTH_LONG
                            ).show()
                        }
            } finally {
                inputStream?.close()
            }
        }
    }
}